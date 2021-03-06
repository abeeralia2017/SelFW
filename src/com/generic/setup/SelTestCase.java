package com.generic.setup;


import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

//import org.apache.log4j.Logger;

import com.generic.util.SASLogger;

import org.testng.Assert;
import org.testng.annotations.*;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.asserts.SoftAssert;
import org.testng.util.Strings;
import org.testng.xml.XmlTest;

import com.generic.util.ReportAnalyzer;
import com.generic.util.ReportUtil;
import com.generic.util.TestUtilities;
import com.generic.util.Xls_Reader;
import com.generic.util.reportBuilder;

public class SelTestCase {
	
	public static String time_date_format = "hh:mm:ss";
	public static String time_date_formatScreenshot = "hhmmssaaa";
	public static String reportFolderDateStampFormat = "MM-dd-yyyy";
	public static String reportFolderTimeStampFormat = "HHmmss";
	public static String testCaseRunDateStamp = "dd.MMMMM.yyyy";

    public static boolean runReportSetup = true;
    public static String mainReportDir = null;
    public static String reportDirectory = null;
    
    public static SASLogger logs = new SASLogger("Default");
    
    protected SoftAssert softAssert = new SoftAssert();
    
    //private static ThreadLocal<String> testName= new ThreadLocal<String>(); 
    
    public static int counter ;
    private static Properties CONFIG = null;
    
    //private WebDriver driver = null;
    private static ThreadLocal<WebDriver> webDriver = new ThreadLocal<WebDriver>();
    
    private static ThreadLocal<SASLogger>TestLog = new ThreadLocal<SASLogger>();
    
    private static int waitTime;
    private static Xls_Reader datatable = null;
    protected static String testCaseReportName = "";
    private static String startTime = null;
    private static String testStatus = null;
    private static String screenShotName = null;
    private static String browserName = null;
    private static int testCaseRowNum;
    private static Throwable Error = null;
    
    
    public static String logDir= null;
    
    public static String testCaseRepotId = null;
    public static String testCaseDescription = null;
    
    public static int caseIndex;
    
    public static String rUNDATE = ReportUtil.now(time_date_format).toString();

    public static String getBrowserName() {
        return browserName;
    }

    public static void setBrowserName(String browserName) {
        SelTestCase.browserName = browserName;
    }

    public static String getScreenShotName() {
        return screenShotName;
    }


    public static void setScreenShotName(String screenShotName) {
        SelTestCase.screenShotName = screenShotName;
    }


    public static String getTestStatus() {
        return testStatus;
    }


    public static void setTestStatus(String testStatus) {
        SelTestCase.testStatus = testStatus;
    }

    public static String getStartTime() {
        return startTime;
    }


    public static void setStartTime(String startTime) {
        SelTestCase.startTime = startTime;
    }

    public static Xls_Reader getDatatable() {
        return datatable;
    }


    public static void setDatatable(Xls_Reader datatable) {
        SelTestCase.datatable = datatable;
    }
    
    public static String getTestCaseDescription() {
        return testCaseDescription;
    }


    public static void setTestCaseDescription(String testDescription) {
        testCaseDescription = testDescription;
    }

    public static int getWaitTime() {
        return waitTime;
    }

    public static void setWaitTime(int time) {
        waitTime = time;
    }

    public static Properties getCONFIG() {
        return CONFIG;
    }

    public static void setCONFIG(Properties cONFIG) {
        CONFIG = cONFIG;
    }


    public static WebDriver getDriver() {
    	return webDriver.get();
    }

    public static void setDriver(WebDriver driver) {
    	webDriver.set(driver);
    }

    public static int getTestCaseRowNum() {
        return testCaseRowNum;
    }


    public static void setTestCaseRowNum(int testCaseRowNum) {
        SelTestCase.testCaseRowNum = testCaseRowNum;
    }
    
    public static String getTestCaseReportName() {
    	if ("".equals(testCaseReportName))
    		return "Automation_Case";
    	return testCaseReportName;
    }

    public static void setTestCaseReportName(String testId) {
        testCaseReportName = testId;
    }

    public static void logCaseDetailds(String msg)
    {
    	logs.debug("Case started: "+Thread.currentThread().getStackTrace()[2]);
    	logs.debug("Case Description: "+msg);
    }
    
    public static void getCurrentFunctionName(Boolean start)
    {
    	if (start){
        	logs.debug(MessageFormat.format(LoggingMsg.STARTING_THREAD, Thread.currentThread().getStackTrace()[2]));
        	}
        	else
        	{
        		logs.debug(MessageFormat.format(LoggingMsg.ENDING_THREAD, Thread.currentThread().getStackTrace()[2]));
        	}
    	
	}
    
    
    
    public static  SASLogger getlogger()
    {
    	return TestLog.get();
    }
    public static  void setlogger(SASLogger log)
    {
    	TestLog.set(log);
    }
    
    /**setUp function will be invoked by Junit before execution of every test case.
     * It initializes the property files and html report setup
     * @throws Exception
     */
    @BeforeMethod
    public void setUp(XmlTest test) throws Exception  {
    	getCurrentFunctionName(true);
        try {
        	if ("".equals(testCaseRepotId))
        		setTestCaseReportName(test.getName());
        	else
        		setTestCaseReportName(testCaseRepotId);
        	
            //Initialize the property file
            TestUtilities.configInitialization();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }

        setTestCaseDescription(getDatatable().getCellData("Test Cases","Description", getTestCaseRowNum()));
        setDriver(Common.initializeBrowser(test.getParameter("browserName")));
        
        try {
        	Common.launchApplication();

        } catch(Throwable t) {
            Error = t;
            logs.debug(MessageFormat.format(LoggingMsg.DEBUGGING_TEXT, t));
            throw new Exception(t);
        }
        getCurrentFunctionName(false);
        logs.debug("Driver Details: "+getDriver());
    }


    /**tearDown function will be invoked by Junit after execution of every test case.
     * It adds the test case status into the html report and close the browser.
     * It reads email id from config file. If they are mentioned then framework will zip the html reports
     * and send it into corresponding email ids.
     * @throws Exception
     */
    @AfterMethod
    public void tearDown() {
    	getCurrentFunctionName(true);
    	
    	WebDriver driver = SelTestCase.getDriver();
        if (driver != null) {
            driver.quit();
        }
        getCurrentFunctionName(false);
    }
    
    
    @AfterSuite
    public static void reportMaker()
    {
    	ArrayList<HashMap<String, String>> casesDetails = null;
    	try {
			TestUtilities.reportSetup();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	ReportAnalyzer.splitLogs();
    	ReportAnalyzer.splitCases();
    	ReportAnalyzer.copyScreenShots();
    	try {
			casesDetails = reportBuilder.readLogs(reportDirectory);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	int passedNumber = 0, failedNumber = 0, skippedNumber = 0;
    	
    	for (HashMap<String, String> status: casesDetails)
    	{
    		if ("pass".contains(status.get("Status").toLowerCase())){
    			passedNumber++;
    		}else if ("failed".contains(status.get("Status").toLowerCase())) {
				failedNumber++;
			}else
			{
				skippedNumber++;
			}
    		logs.debug(Arrays.asList(status)+"");
    		ReportUtil.addTestCase(status.get("TestName"), status.get("Details"),status.get("StartTime"),
    				status.get("EndTime"),status.get("Status"),
    				status.get("LogFileName"), status.get("Browser"));
    	}
    	
    	ReportUtil.updateEndTime(ReportUtil.now(time_date_format), getTestStatus());
    	
    	ReportUtil.endSuite(passedNumber, failedNumber, skippedNumber);
    }
}
