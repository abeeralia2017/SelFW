package com.generic.tests.Cart;

import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;
import org.junit.Assert;
import org.junit.Test;
import com.generic.page.Cart;
import com.generic.setup.Common;
import com.generic.setup.LoggingMsg;
import com.generic.util.ReportUtil;

public class CartValidationRemoveProduct extends CartValidationBase {

	@Test
	public void signIn() throws Exception {
		setStartTime(ReportUtil.now(time_date_format));

		try {
			prepareCartLoggedInUser();
			logs.debug(Cart.getNumberOfproducts());
			Cart.removeItemFromCart(0);
			assertTrue(Cart.isCartEmpty());
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
	
	@Test
	public void NotsignIn() throws Exception {
		setStartTime(ReportUtil.now(time_date_format));

		try {
			prepareCartNotLoggedInUser();
			logs.debug(Cart.getNumberOfproducts());
			Cart.removeItemFromCart(0);
			assertTrue(Cart.isCartEmpty());
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
