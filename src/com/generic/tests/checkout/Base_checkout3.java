package com.generic.tests.checkout;

import static org.testng.Assert.assertEquals;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import java.util.LinkedHashMap;

import com.generic.page.PDP;
import com.generic.page.Registration;
import com.generic.page.Cart;
import com.generic.page.CheckOut;
import com.generic.page.SignIn;
import com.generic.setup.Common;
import com.generic.setup.LoggingMsg;
import com.generic.setup.SelTestCase;
import com.generic.setup.SheetVariables;
import com.generic.util.TestUtilities;
import com.generic.util.Xls_Reader;
import com.generic.util.RandomUtilities;
import com.generic.util.ReportUtil;
import com.generic.util.SASLogger;

import org.testng.Reporter;

public class Base_checkout3 extends SelTestCase {

	private static LinkedHashMap<String, Object> addresses = null ;
	private static  LinkedHashMap<String, Object> invintory = null ;
	private static  LinkedHashMap<String, Object> paymentCards = null;
	private static  LinkedHashMap<String, Object> users =null ;

	// user types
	public static final String guestUser = "guest";
	public static final String freshUser = "fresh";
	public static final String loggedInUser = "loggedin";

	// used sheet in test
	public static final String testDataSheet = "CheckOutRegressionTMP_1";

	private int caseIndexInDatasheet;
	private String email;
	private String orderId;
	private String orderTotal;
	private String orderSubtotal;
	private String orderTax;
	private String orderShipping;
	
	private static XmlTest testObject;
	
	private static ThreadLocal<SASLogger> Testlogs = new ThreadLocal<SASLogger>() ; 
	
	@BeforeTest
	public static void initialSetUp(XmlTest test) throws Exception {
		//testCaseRepotId = SheetVariables.checkoutTestCaseId;
//		logs = new SASLogger(test.getName());
//    	setlogger(logs);
//    	
//    	logs.debug(test.getName());
		
		Testlogs.set(new SASLogger(test.getName()+test.getIndex()));
		testObject = test;
		addresses = Common.readAddresses();
		invintory = Common.readLocalInventory();
		paymentCards = Common.readPaymentcards();
		users = Common.readUsers();
	}

	@DataProvider(name = "Orders", parallel = false)
	public static Object[][] loadTestData() throws Exception {
		Object[][] data = TestUtilities.getData(testDataSheet);
		Testlogs.get().debug(Arrays.deepToString(data).replace("\n", "--"));
		return data;
	}
	
	//@Test(dataProvider = "Orders", threadPoolSize = 5, invocationCount = 1,  timeOut = 10000)
	public void dummy(String caseId, String runTest, String desc, String proprties, String products,
			String shippingMethod, String payment, String shippingAddress, String billingAddress, String coupon,
			String email, String orderId, String orderTotal, String orderSubtotal, String orderTax,
			String orderShipping) throws InterruptedException
	{
		//Reporter.log(Thread.currentThread().getId()+"", true);
		Testlogs.get().debug("sdfsdfsdfsdfsdfsdfsdfsdfsdfsdfd");
		System.out.println( Thread.currentThread().getId()+ "");
		Testlogs.get().debug("=============>"+SelTestCase.getDriver().toString());
		getDriver().findElement(By.id("test_loginAndCheckoutButton_$2")).click();
		Thread.sleep(10000);
		Common.testPass();
	}

	@SuppressWarnings("unchecked") // avoid warning from linked hashmap
	@Test(dataProvider = "Orders")
	public void checkOutBaseTest(String caseId, String runTest, String desc, String proprties, String products,
			String shippingMethod, String payment, String shippingAddress, String billingAddress, String coupon,
			String email, String orderId, String orderTotal, String orderSubtotal, String orderTax,
			String orderShipping) throws Exception {
		//Important to add this for logging/reporting 
		setTestCaseReportName("Checkout Case");
		Testlogs.get().debug("Case Browser: "  + testObject.getParameter("browserName") );
		logCaseDetailds(MessageFormat.format(LoggingMsg.CHECKOUTDESC, testDataSheet + "." + caseId,
				this.getClass().getCanonicalName(), desc, proprties.replace("\n", "<br>- "), payment, shippingMethod));
		Testlogs.get().debug("in basecheckout" );
		
		
		this.email = email;
		caseIndexInDatasheet = getDatatable().getCellRowNum(testDataSheet, CheckOut.keys.caseId, caseId);
//		initializeTestResults(testDataSheet, caseIndexInDatasheet);
		
		try {
//			Thread.sleep(10000);
			if (proprties.contains(loggedInUser)) {
				LinkedHashMap<String, Object> userdetails = (LinkedHashMap<String, Object>) users.get(this.email);
				Testlogs.get().debug(this.email );
				Testlogs.get().debug((String) userdetails.get(Registration.keys.password) );
				SignIn.logIn(this.email, (String) userdetails.get(Registration.keys.password));
			}
			if (proprties.contains(freshUser)) {
				this.email = RandomUtilities.getRandomEmail();

				// take any user as template
				LinkedHashMap<String, Object> userdetails = (LinkedHashMap<String, Object>) users.entrySet().iterator()
						.next().getValue();
				// userdetails.put(Registration.keys.email,email); //TODO: remove it

				boolean acceptRegTerm = true;

				Registration.fillAndClickRegister((String) userdetails.get(Registration.keys.title),
						(String) userdetails.get(Registration.keys.firstName),
						(String) userdetails.get(Registration.keys.lastName), this.email,
						(String) userdetails.get(Registration.keys.password),
						(String) userdetails.get(Registration.keys.password), acceptRegTerm);
			}

			for (String product : products.split("\n")) {
				Testlogs.get().debug(MessageFormat.format(LoggingMsg.ADDING_PRODUCT, product));
				LinkedHashMap<String, Object> productDetails = (LinkedHashMap<String, Object>) invintory.get(product);
				PDP.addProductsToCart((String) productDetails.get(PDP.keys.url),
						(String) productDetails.get(PDP.keys.color), (String) productDetails.get(PDP.keys.size),
						(String) productDetails.get(PDP.keys.qty));
				ReportUtil.takeScreenShot(getDriver());
			}

			// flow to support coupon validation
			if (!"".equals(coupon)) {
				Cart.applyCoupon(coupon);
				if (coupon.contains(Cart.keys.invalidCoupon)) {
					Cart.validateinvaldcoupon();
				}
			}
			Cart.getNumberOfproducts();
			this.orderSubtotal = Cart.getOrderSubTotal();
			this.orderTax = Cart.getOrderTax();

			Cart.clickCheckout();

			if (proprties.contains(guestUser)) {
				this.email = RandomUtilities.getRandomEmail();
				CheckOut.guestCheckout.fillAndClickGuestCheckout(this.email);
			}

			// Validate the order sub total in shipping address form section
			softAssert.assertEquals(CheckOut.shippingAddress.getOrdersubTotal(), this.orderSubtotal);

			// checkout- shipping address
			if (proprties.contains(CheckOut.shippingAddress.keys.isSavedShipping) && !proprties.contains(freshUser)
					&& !proprties.contains(guestUser)) {
				CheckOut.shippingAddress.fillAndClickNext(true);
			} else {
				LinkedHashMap<String, Object> addressDetails = (LinkedHashMap<String, Object>) addresses
						.get(shippingAddress);

				boolean saveShipping = !proprties.contains(guestUser);

				// in case guest the save shipping checkbox is not exist
				if (saveShipping) {
					CheckOut.shippingAddress.fillAndClickNext(
							(String) addressDetails.get(CheckOut.shippingAddress.keys.countery),
							(String) addressDetails.get(CheckOut.shippingAddress.keys.title),
							(String) addressDetails.get(CheckOut.shippingAddress.keys.firstName),
							(String) addressDetails.get(CheckOut.shippingAddress.keys.lastName),
							(String) addressDetails.get(CheckOut.shippingAddress.keys.adddressLine),
							(String) addressDetails.get(CheckOut.shippingAddress.keys.city),
							(String) addressDetails.get(CheckOut.shippingAddress.keys.postal),
							(String) addressDetails.get(CheckOut.shippingAddress.keys.phone), saveShipping);
				} else {
					CheckOut.shippingAddress.fillAndClickNext(
							(String) addressDetails.get(CheckOut.shippingAddress.keys.countery),
							(String) addressDetails.get(CheckOut.shippingAddress.keys.title),
							(String) addressDetails.get(CheckOut.shippingAddress.keys.firstName),
							(String) addressDetails.get(CheckOut.shippingAddress.keys.lastName),
							(String) addressDetails.get(CheckOut.shippingAddress.keys.adddressLine),
							(String) addressDetails.get(CheckOut.shippingAddress.keys.city),
							(String) addressDetails.get(CheckOut.shippingAddress.keys.postal),
							(String) addressDetails.get(CheckOut.shippingAddress.keys.phone));
				}
			}

			// Validate the order sub total in shipping method section
			softAssert.assertEquals(CheckOut.shippingMethod.getOrderSubTotal(), this.orderSubtotal);

			// Shipping method
			CheckOut.shippingMethod.fillAndclickNext(shippingMethod);

			// Validate the order sub total in billing form section
			softAssert.assertEquals(CheckOut.paymentInnformation.getOrderSubTotal(), this.orderSubtotal);

			// checkout- payment
			if (proprties.contains(CheckOut.paymentInnformation.keys.isSavedPayement) && !proprties.contains(freshUser)
					&& !proprties.contains(guestUser)) {
				CheckOut.paymentInnformation.fillAndclickNext(true);
			} else {

				// do not save address if scenario is guest user
				boolean saveBilling = !proprties.contains(guestUser);
				LinkedHashMap<String, Object> paymentDetails = (LinkedHashMap<String, Object>) paymentCards
						.get(payment);
				LinkedHashMap<String, Object> billAddressDetails = (LinkedHashMap<String, Object>) addresses
						.get(billingAddress);

				if (saveBilling) {
					CheckOut.paymentInnformation.fillAndclickNext(payment,
							(String) paymentDetails.get(CheckOut.paymentInnformation.keys.name),
							(String) paymentDetails.get(CheckOut.paymentInnformation.keys.number),
							(String) paymentDetails.get(CheckOut.paymentInnformation.keys.expireMonth),
							(String) paymentDetails.get(CheckOut.paymentInnformation.keys.expireYear),
							(String) paymentDetails.get(CheckOut.paymentInnformation.keys.CVCC), saveBilling,
							billingAddress.equalsIgnoreCase(shippingAddress),
							(String) billAddressDetails.get(CheckOut.shippingAddress.keys.countery),
							(String) billAddressDetails.get(CheckOut.shippingAddress.keys.title),
							(String) billAddressDetails.get(CheckOut.shippingAddress.keys.firstName),
							(String) billAddressDetails.get(CheckOut.shippingAddress.keys.lastName),
							(String) billAddressDetails.get(CheckOut.shippingAddress.keys.adddressLine),
							(String) billAddressDetails.get(CheckOut.shippingAddress.keys.city),
							(String) billAddressDetails.get(CheckOut.shippingAddress.keys.postal),
							(String) billAddressDetails.get(CheckOut.shippingAddress.keys.phone));
				} else {
					CheckOut.paymentInnformation.fillAndclickNext(payment,
							(String) paymentDetails.get(CheckOut.paymentInnformation.keys.name),
							(String) paymentDetails.get(CheckOut.paymentInnformation.keys.number),
							(String) paymentDetails.get(CheckOut.paymentInnformation.keys.expireMonth),
							(String) paymentDetails.get(CheckOut.paymentInnformation.keys.expireYear),
							(String) paymentDetails.get(CheckOut.paymentInnformation.keys.CVCC),
							billingAddress.equalsIgnoreCase(shippingAddress),
							(String) billAddressDetails.get(CheckOut.shippingAddress.keys.countery),
							(String) billAddressDetails.get(CheckOut.shippingAddress.keys.title),
							(String) billAddressDetails.get(CheckOut.shippingAddress.keys.firstName),
							(String) billAddressDetails.get(CheckOut.shippingAddress.keys.lastName),
							(String) billAddressDetails.get(CheckOut.shippingAddress.keys.adddressLine),
							(String) billAddressDetails.get(CheckOut.shippingAddress.keys.city),
							(String) billAddressDetails.get(CheckOut.shippingAddress.keys.postal),
							(String) billAddressDetails.get(CheckOut.shippingAddress.keys.phone));
				}
			}

			// Validate the order subtotal in order review section
			softAssert.assertEquals(CheckOut.reviewInformation.getSubtotal(), this.orderSubtotal);

			CheckOut.reviewInformation.acceptTerms(true);
			CheckOut.reviewInformation.placeOrder();

			// Validate the order sub total in order review section
			softAssert.assertEquals(CheckOut.orderConfirmation.getSubTotal(), this.orderSubtotal);

			this.orderTotal = CheckOut.orderConfirmation.getOrderTotal();
			this.orderShipping = CheckOut.orderConfirmation.getShippingCost();
			this.orderId = CheckOut.orderConfirmation.getOrderId();

			// TODO: compare addresses
			CheckOut.orderConfirmation.getBillingAddrerss();
			CheckOut.orderConfirmation.getShippingAddrerss();

			if (proprties.contains(guestUser) && proprties.contains("register-guest")) {
				CheckOut.guestCheckout.fillPreRegFormAndClickRegBtn("1234567", false);
			}

			//writeResultsToTestDatasheet(testDataSheet, caseIndexInDatasheet);
			Common.testPass();
			ReportUtil.takeScreenShot(getDriver());
		} catch (Throwable t) {
			setTestCaseDescription(getTestCaseDescription());
			Testlogs.get().debug(MessageFormat.format(LoggingMsg.DEBUGGING_TEXT, t.getMessage()));
			t.printStackTrace();
			String temp = getTestCaseReportName();
			Common.testFail(t, temp);
			ReportUtil.takeScreenShot(getDriver());
			Assert.assertTrue(false, t.getMessage());
		} // catch
	}// test

//	private void writeResultsToTestDatasheet(String sheetName, int row) {
//		getCurrentFunctionName(true);
//		getDatatable().setCellData(sheetName, CheckOut.orderConfirmation.keys.orderId, row, orderId);
//		if (email.contains("random")) {
//			getDatatable().setCellData(sheetName, CheckOut.orderConfirmation.keys.email, row, email);
//		}
//		getDatatable().setCellData(sheetName, CheckOut.orderConfirmation.keys.orderSubtotal, row, orderSubtotal);
//		getDatatable().setCellData(sheetName, CheckOut.orderConfirmation.keys.orderShipping, row, orderShipping);
//		getDatatable().setCellData(sheetName, CheckOut.orderConfirmation.keys.orderTax, row, orderTax);
//		getDatatable().setCellData(sheetName, CheckOut.orderConfirmation.keys.orderTotal, row, orderTotal);
//		getCurrentFunctionName(false);
//	}// write results
//
//	private void initializeTestResults(String sheetName, int row) {
//		getCurrentFunctionName(true);
//		getDatatable().setCellData(sheetName, CheckOut.orderConfirmation.keys.orderId, row, "");
//		if (email.contains("random")) {
//			getDatatable().setCellData(sheetName, CheckOut.orderConfirmation.keys.email, row, "");
//		}
//		getDatatable().setCellData(sheetName, CheckOut.orderConfirmation.keys.orderShipping, row, "");
//		getDatatable().setCellData(sheetName, CheckOut.orderConfirmation.keys.orderSubtotal, row, "");
//		getDatatable().setCellData(sheetName, CheckOut.orderConfirmation.keys.orderTax, row, "");
//		getDatatable().setCellData(sheetName, CheckOut.orderConfirmation.keys.orderTotal, row, "");
//		getCurrentFunctionName(false);
//	}// initializeTestResults
}// class
