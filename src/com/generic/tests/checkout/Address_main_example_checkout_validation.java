package com.generic.tests.checkout;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.generic.setup.Common;
import com.generic.setup.LoggingMsg;
import com.generic.setup.SelTestCase;
import com.generic.setup.SheetVariables;
import com.generic.util.ReportUtil;
import com.generic.util.TestUtilities;

public class Address_main_example_checkout_validation extends SelTestCase {

	private static int testCaseID;

	static List<String> subStrArr = new ArrayList<String>();
	static List<String> valuesArr = new ArrayList<String>();

	@BeforeClass
	public static void initialSetUp() throws Exception {
		testCaseRepotId = SheetVariables.checkoutTestCaseId + "_" + testCaseID;
		caseIndex = 2;
		TestUtilities.configInitialization();
	}

	
	public void dummy() throws InterruptedException
	{
		//Reporter.log(Thread.currentThread().getId()+"", true);
		logs.debug("sdfsdfsdfsdfsdfsdfsdfsdfsdfsdfd");
		logs.debug("=============>"+getDriver().toString());
		getDriver().findElement(By.id("test_loginAndCheckoutButton_$2")).click();
		Thread.sleep(10000);
		Common.testPass();
	}
	
	@Test
	public void signIn() throws Exception {
		setStartTime(ReportUtil.now(time_date_format));

		try {
			
			//read addresses
			LinkedHashMap<String, Object> addresses = new LinkedHashMap<>();
			addresses = Common.readAddresses();
			logs.debug(MessageFormat.format(LoggingMsg.DEBUGGING_TEXT, Arrays.asList(addresses)));
			
			//read products
			LinkedHashMap<String, Object> products = new LinkedHashMap<>();
			products = Common.readLocalInventory();
			logs.debug(MessageFormat.format(LoggingMsg.DEBUGGING_TEXT, Arrays.asList(products)));
			
			//read payments
			LinkedHashMap<String, Object> payments = new LinkedHashMap<>();
			payments = Common.readPaymentcards();
			logs.debug(MessageFormat.format(LoggingMsg.DEBUGGING_TEXT, Arrays.asList(payments)));
			
			//Parameterized class with flow control 
			
			
			Common.testPass();
		} catch (Throwable t) {
			setTestCaseDescription(getTestCaseDescription());
			logs.debug(MessageFormat.format(LoggingMsg.DEBUGGING_TEXT, t.getMessage()));
			t.printStackTrace();
			String temp = getTestCaseReportName();
			Common.testFail(t, temp);
			Common.takeScreenShot();
			Assert.assertTrue(t.getMessage(), false);
		}

	}

}
