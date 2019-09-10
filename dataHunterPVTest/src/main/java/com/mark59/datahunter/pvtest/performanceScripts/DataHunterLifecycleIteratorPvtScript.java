/*
 *  Copyright 2019 Insurance Australia Group Limited
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mark59.datahunter.pvtest.performanceScripts;


import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;

import com.mark59.core.Outcome;
import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.datahunter.pvtest.dsl.helperBeans.Policies;
import com.mark59.datahunter.pvtest.dsl.helperBeans.PolicySelectionCriteria;
import com.mark59.datahunter.pvtest.dsl.pageobjects.AddPolicyActionPage;
import com.mark59.datahunter.pvtest.dsl.pageobjects.AddPolicyPage;
import com.mark59.datahunter.pvtest.dsl.pageobjects.CountPoliciesActionPage;
import com.mark59.datahunter.pvtest.dsl.pageobjects.CountPoliciesBreakdownActionPage;
import com.mark59.datahunter.pvtest.dsl.pageobjects.CountPoliciesBreakdownPage;
import com.mark59.datahunter.pvtest.dsl.pageobjects.CountPoliciesPage;
import com.mark59.datahunter.pvtest.dsl.pageobjects.DeleteMultiplePoliciesActionPage;
import com.mark59.datahunter.pvtest.dsl.pageobjects.DeleteMultiplePoliciesPage;
import com.mark59.datahunter.pvtest.dsl.pageobjects.NextPolicyActionPage;
import com.mark59.datahunter.pvtest.dsl.pageobjects.NextPolicyPage;
import com.mark59.datahunter.pvtest.dsl.pageobjects.SuperDataHunterActionPage;
import com.mark59.datahunter.pvtest.dsl.utils.DirectPageGets;
import com.mark59.datahunter.pvtest.dsl.utils.DslConstants;
import com.mark59.selenium.corejmeterimpl.JmeterFunctionsForSeleniumScripts;
import com.mark59.selenium.corejmeterimpl.KeepBrowserOpen;
import com.mark59.selenium.corejmeterimpl.SeleniumIteratorAbstractJavaSamplerClient;
import com.mark59.selenium.drivers.SeleniumDriverFactory;

//import com.mark59.selenium.drivers.Mark59LogLevels;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class DataHunterLifecycleIteratorPvtScript  extends SeleniumIteratorAbstractJavaSamplerClient {

	private static final Logger LOG = Logger.getLogger(DataHunterLifecycleIteratorPvtScript.class);	

	private String application;
	private String lifecycle;
	private String user = "default_user";
	private PolicySelectionCriteria policySelectionCriteria = new PolicySelectionCriteria();
	private DirectPageGets directPageGets;
	int forceTxnFailPercent;
	
	@Override
	protected Map<String, String> additionalTestParameters() {
		Map<String, String> jmeterAdditionalParameters = new LinkedHashMap<String, String>();
		
		jmeterAdditionalParameters.put(ITERATE_FOR_PERIOD_IN_SECS, 						"25");
		jmeterAdditionalParameters.put(ITERATE_FOR_NUMBER_OF_TIMES,  					 "0");
		jmeterAdditionalParameters.put(ITERATION_PACING_IN_SECS,  						"10");
		jmeterAdditionalParameters.put(STOP_THREAD_AFTER_TEST_START_IN_SECS,  			 "0");
		jmeterAdditionalParameters.put(TestConstants.PARM_NAME_DATAHUNTER_URL_HOST_PORT,  DslConstants.DEFAULT_DATAHUNTER_URL_HOST_PORT);
		jmeterAdditionalParameters.put(TestConstants.PARM_NAME_DATAHUNTER_APPLICATION_ID, TestConstants.DATAHUNTER_PV_TEST);
		jmeterAdditionalParameters.put(TestConstants.PARM_NAME_FORCE_TXN_FAIL_PERCENT, 	"20");
		jmeterAdditionalParameters.put("USER", 	user);
		jmeterAdditionalParameters.put("DRIVER", "CHROME");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.HEADLESS_MODE, String.valueOf(false));
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PAGE_LOAD_STRATEGY, PageLoadStrategy.NORMAL.toString());
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PROXY, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.ADDITIONAL_OPTIONS, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.WRITE_FFOX_BROWSER_LOGFILE, 	String.valueOf(false));
		jmeterAdditionalParameters.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");
		return jmeterAdditionalParameters;			
	}
	
	
	/**
	 *  Initiate does a data clean-up (typically could also be an application logon)
	 */
	@Override
	protected void initiateSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm, WebDriver driver) {
		String thread = Thread.currentThread().getName(); 
		LOG.debug( "executing initiateSeleniumTest Thread: " + thread); 
		
//		jm.logScreenshotsAtStartOfTransactions(Mark59LogLevels.WRITE);
//		jm.logScreenshotsAtEndOfTransactions(Mark59LogLevels.WRITE);
//		jm.logPageSourceAtStartOfTransactions(Mark59LogLevels.WRITE);		
//		jm.logPageSourceAtEndOfTransactions(Mark59LogLevels.WRITE );
//		jm.logPerformanceLogAtEndOfTransactions(Mark59LogLevels.WRITE);
//		jm.logAllLogsAtEndOfTransactions(Mark59LogLevels.BUFFER);	
		
		user = context.getParameter("USER");
		
		forceTxnFailPercent = new Integer(context.getParameter(TestConstants.PARM_NAME_FORCE_TXN_FAIL_PERCENT).trim());
		LOG.debug( "forceTxnFailPercent : " + forceTxnFailPercent); 
		
		application = context.getParameter(TestConstants.PARM_NAME_DATAHUNTER_APPLICATION_ID);
		lifecycle = "thread_" + thread;

		policySelectionCriteria.setApplication(application);
		policySelectionCriteria.setLifecycle(lifecycle);
		policySelectionCriteria.setUseability(DslConstants.UNSELECTED);		
		
		String dataHunterUrl = context.getParameter(TestConstants.PARM_NAME_DATAHUNTER_URL_HOST_PORT);
		directPageGets = new DirectPageGets(driver, dataHunterUrl, policySelectionCriteria.getApplication()); 

		
// 		delete multiple policies (for policies created using this threadname) 
		directPageGets.gotoDeleteMultiplePoliciesPage();
		DeleteMultiplePoliciesPage deleteMultiplePoliciesPage = new DeleteMultiplePoliciesPage(driver); 
		deleteMultiplePoliciesPage.lifecycle.type(policySelectionCriteria.getLifecycle());

		jm.startTransaction("DH-lifecycle-0001-initiate-deleteMultiplePolicies");		
		deleteMultiplePoliciesPage.submit.submit();
		DeleteMultiplePoliciesActionPage deleteMultiplePoliciesActionPage = new DeleteMultiplePoliciesActionPage(driver);
		String sqlResultText = deleteMultiplePoliciesActionPage.sqlResult.getText();
		checkSqlOk(sqlResultText, deleteMultiplePoliciesActionPage);
		jm.endTransaction("DH-lifecycle-0001-initiate-deleteMultiplePolicies");	
	}

	

	/**
	 * Iterate over a typical DataHunter lifecycle 
	 */
	@Override
	protected void iterateSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm,  WebDriver driver) {

// 		delete multiple policies (for policies created using this threadname) 
		directPageGets.gotoDeleteMultiplePoliciesPage();
		DeleteMultiplePoliciesPage deleteMultiplePoliciesPage = new DeleteMultiplePoliciesPage(driver); 
		deleteMultiplePoliciesPage.lifecycle.type(policySelectionCriteria.getLifecycle());

		jm.startTransaction("DH-lifecycle-0100-deleteMultiplePolicies");		
		deleteMultiplePoliciesPage.submit.submit();
		DeleteMultiplePoliciesActionPage deleteMultiplePoliciesActionPage = new DeleteMultiplePoliciesActionPage(driver);
		String sqlResultText = deleteMultiplePoliciesActionPage.sqlResult.getText();
		checkSqlOk(sqlResultText, deleteMultiplePoliciesActionPage);
		jm.endTransaction("DH-lifecycle-0100-deleteMultiplePolicies");	
		
// 		add some policies (put the user in 'otherdata') 
		Policies policy = new Policies(application, null, lifecycle, DslConstants.UNUSED, user, 0L);
		AddPolicyActionPage addPolicyActionPage;
		directPageGets.gotoAddPolicyPage();
		AddPolicyPage addPolicyPage = new AddPolicyPage(driver);
		
		for (int i = 1; i <= 5; i++) {
			policy.setIdentifier("TESTID" + i);
			policy.setEpochtime(System.currentTimeMillis());			
			addPolicyPage = new AddPolicyPage(driver);
			addPolicyPage.identifier.type(policy.getIdentifier());
			addPolicyPage.lifecycle.type(policy.getLifecycle());
			addPolicyPage.useability.selectByVisibleText(policy.getUseability());
			addPolicyPage.otherdata.type(policy.getOtherdata());		
			addPolicyPage.epochtime.type(policy.getEpochtime().toString());
			
			// uncomment to see all the policies being created:
			//jm.writeScreenshot("add_policy_" + policy.getIdentifier());			
			
			jm.startTransaction("DH-lifecycle-0200-addPolicy");
			addPolicyPage.submit.submit();	
			addPolicyActionPage = new AddPolicyActionPage(driver);
			checkSqlOk(addPolicyActionPage.sqlResult.getText(), deleteMultiplePoliciesActionPage);
			
			//sample usage of Link waits (the Back link on the addPolicyActionPage, the Home Page link on the addPolicyPage) 
			
			addPolicyActionPage.backLink.click();  					
			addPolicyPage = new AddPolicyPage(driver);
//			addPolicyPage = new AddPolicyPage(driver, true);     // Selenium wait for conditions in 'debug' modes:
			addPolicyPage.homePageLink().waitUntilClickable();   
			jm.endTransaction("DH-lifecycle-0200-addPolicy");
		} 
				
//		if (Thread.currentThread().getName().equals("main")){
//			System.out.println("SIMULATING FAILURE ON THREAD " + Thread.currentThread().getName());
//			throw new RuntimeException(" -- simulate failure on " + Thread.currentThread().getName() +" -- ");
//		}
		
//		dummy transaction just to test transaction failure behaviour 		
		jm.startTransaction("DH-lifecycle-0299-sometimes-I-fail");
		int randomNum_1_to_100 = ThreadLocalRandom.current().nextInt(1, 101);
		if ( randomNum_1_to_100 >= forceTxnFailPercent ) {
			jm.endTransaction("DH-lifecycle-0299-sometimes-I-fail", Outcome.PASS);
		} else {
			jm.endTransaction("DH-lifecycle-0299-sometimes-I-fail", Outcome.FAIL);
		}
		
// 		count all unused DATAHUNTER_PV_TEST policies
		policySelectionCriteria.setApplication(application);		
		policySelectionCriteria.setLifecycle(null);                 // count across all life-cycles    
		policySelectionCriteria.setUseability(DslConstants.UNUSED);	
		
		directPageGets.gotoCountPoliciesPage();
		CountPoliciesPage countPoliciesPage = new CountPoliciesPage(driver); 
		countPoliciesPage.lifecycle.type(policySelectionCriteria.getLifecycle());
		countPoliciesPage.useability.selectByVisibleText(policySelectionCriteria.getUseability());

		jm.startTransaction("DH-lifecycle-0300-countUnusedPolicies");
		countPoliciesPage.submit.submit();
		CountPoliciesActionPage countPoliciesActionPage = new CountPoliciesActionPage(driver);	
		checkSqlOk(countPoliciesActionPage.sqlResult.getText(), countPoliciesActionPage);
		jm.endTransaction("DH-lifecycle-0300-countUnusedPolicies");
	
		
		Long countPolicies = Long.valueOf( countPoliciesActionPage.rowsAffected.getText());
		LOG.debug( "countPolicies : " + countPolicies); 
		
		jm.userDataPoint(application + "_Total_Unused_Policy_Count", countPolicies);
			
// 		count breakdown (count for unused DATAHUNTER_PV_TEST policies for this thread )
		policySelectionCriteria.setUseability("UNUSED");
		policySelectionCriteria.setLifecycle(lifecycle);   		
		
		directPageGets.gotoCountPoliciesBreakdownPage();
		CountPoliciesBreakdownPage countPoliciesBreakdownPage = new CountPoliciesBreakdownPage(driver);
		countPoliciesBreakdownPage.lifecycle.type(policySelectionCriteria.getLifecycle());
		countPoliciesBreakdownPage.useability.selectByVisibleText(policySelectionCriteria.getUseability());
		
		jm.startTransaction("DH-lifecycle-0350-countUnusedPoliciesCurrentThread");		
		countPoliciesBreakdownPage.submit.submit();
		CountPoliciesBreakdownActionPage countPoliciesBreakdownActionPage = new CountPoliciesBreakdownActionPage(driver);	
		checkSqlOk(countPoliciesBreakdownActionPage.sqlResult.getText(), countPoliciesBreakdownActionPage);		
		jm.endTransaction("DH-lifecycle-0350-countUnusedPoliciesCurrentThread");				

		int countUsedPoliciesCurrentThread = countPoliciesBreakdownActionPage.getCountForBreakdown(application, lifecycle, "UNUSED" );
		LOG.debug( "countUsedPoliciesCurrentThread : " + countUsedPoliciesCurrentThread); 
		
		jm.userDataPoint(application + "_This_Thread_Unused_Policy_Count", countUsedPoliciesCurrentThread);		
			
//		lookup next policy
		policySelectionCriteria.setSelectOrder(DslConstants.SELECT_MOST_RECENTLY_ADDED);
		policySelectionCriteria.setLifecycle(lifecycle); 			
		
		directPageGets.gotoNextPolicyPage(DslConstants.LOOKUP);
		NextPolicyPage nextPolicyPage = new NextPolicyPage(driver); 
		nextPolicyPage.lifecycle.type(policySelectionCriteria.getLifecycle());
		nextPolicyPage.useability.selectByVisibleText(policySelectionCriteria.getUseability());
		nextPolicyPage.selectOrder.selectByVisibleText(policySelectionCriteria.getSelectOrder());
		
		jm.startTransaction("DH-lifecycle-0400-lookupNextPolicy");				
		nextPolicyPage.submit.submit();
		NextPolicyActionPage nextPolicyActionPage = new NextPolicyActionPage(driver);		
		checkSqlOk(nextPolicyActionPage.sqlResult.getText(), nextPolicyActionPage);			
		jm.endTransaction("DH-lifecycle-0400-lookupNextPolicy");	

		if (LOG.isDebugEnabled() ) {
			sendPolicyDetailsToLog("lookupNextPolicy", nextPolicyActionPage); 
		}
		
//		use next policy
		policySelectionCriteria.setSelectOrder(DslConstants.SELECT_MOST_RECENTLY_ADDED);
		directPageGets.gotoNextPolicyPage(DslConstants.USE);
		nextPolicyPage = new NextPolicyPage(driver); 
		nextPolicyPage.lifecycle.type(policySelectionCriteria.getLifecycle());
		nextPolicyPage.useability.selectByVisibleText(policySelectionCriteria.getUseability());
		nextPolicyPage.selectOrder.selectByVisibleText(policySelectionCriteria.getSelectOrder());
		
		jm.startTransaction("DH-lifecycle-0500-useNextPolicy");		
		nextPolicyPage.submit.submit();
		nextPolicyActionPage = new NextPolicyActionPage(driver);		
		checkSqlOk(nextPolicyActionPage.sqlResult.getText(), nextPolicyActionPage);			
		jm.endTransaction("DH-lifecycle-0500-useNextPolicy");	
		
		if (LOG.isDebugEnabled() ) {
			sendPolicyDetailsToLog("useNextPolicy", nextPolicyActionPage); 
		}
//		jm.writeBufferedArtifacts();
	}

	
	/**
	 *  Finalize does a data clean-up (typically could also be an application logoff)
	 */
	@Override
	protected void finalizeSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm,	WebDriver driver) {
		directPageGets.gotoDeleteMultiplePoliciesPage();
		DeleteMultiplePoliciesPage deleteMultiplePoliciesPage = new DeleteMultiplePoliciesPage(driver); 
		deleteMultiplePoliciesPage.lifecycle.type(policySelectionCriteria.getLifecycle());   // this thread

		jm.startTransaction("DH-lifecycle-9999-finalize-deleteMultiplePolicies");		
		deleteMultiplePoliciesPage.submit.submit();
		DeleteMultiplePoliciesActionPage deleteMultiplePoliciesActionPage = new DeleteMultiplePoliciesActionPage(driver);
		checkSqlOk(deleteMultiplePoliciesActionPage.sqlResult.getText(), deleteMultiplePoliciesActionPage);
		jm.endTransaction("DH-lifecycle-9999-finalize-deleteMultiplePolicies");	
	}

	
	@Override
	protected void userActionsOnScriptFailure(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm,	WebDriver driver) {
		// just as a demo, create some transaction and go to some random page (that is different to the page the simulated crash occurred
		jm.startTransaction("DH-lifecycle-9998-userActionsOnScriptFailure");
		new DirectPageGets(driver, context.getParameter(TestConstants.PARM_NAME_DATAHUNTER_URL_HOST_PORT), policySelectionCriteria.getApplication()).gotoCountPoliciesPage(); 
		System.out.println("   -- page at userActionsOnScriptFailure has been changed to " + driver.getTitle() + " --");
		jm.endTransaction("DH-lifecycle-9998-userActionsOnScriptFailure");
	}
	
	
	private void checkSqlOk(String sqlResultText, SuperDataHunterActionPage actionPaged) {
		if ( ! DslConstants.SQL_RESULT_PASS.equals(sqlResultText) ) {
			throw new RuntimeException("SQL issue (" + sqlResultText + ") : " + actionPaged.formatResultsMessage(actionPaged.getClass().getName()));   
		}
	}
	
	private Policies sendPolicyDetailsToLog(String logMsgPrefix,NextPolicyActionPage nextPolicyActionPage) {
		Policies policy = new Policies();
		policy.setApplication(nextPolicyActionPage.application.getText());
		policy.setIdentifier(nextPolicyActionPage.identifier.getText());
		policy.setLifecycle(nextPolicyActionPage.lifecycle.getText());
		policy.setUseability(nextPolicyActionPage.useability.getText());			
		policy.setOtherdata(nextPolicyActionPage.otherdata.getText());
		policy.setCreated(Timestamp.valueOf(nextPolicyActionPage.created.getText()));
		policy.setUpdated(Timestamp.valueOf(nextPolicyActionPage.updated.getText()));
		policy.setEpochtime(new Long(nextPolicyActionPage.epochtime.getText()));	
		LOG.debug( logMsgPrefix + " : " + policy);
		return policy;
	}

	
	/**
	 * A main method to assist with script testing outside Jmeter.  The samples below demonstrate three ways of running the script: <br><br>
	 * 1.  Run a simple single instance, without extra thread-based parameterization (KeepBrowserOpen enumeration is optionally available).<br>
	 * 2.  Run multiple instances of the script, without extra thread-based parameterization <br> 
	 * 3.  Run multiple instances of the script, with extra thread-based parameterization, represented as a map with parameter name as key, and values for each instance to be executed<br>  
	 * 
	 * For logging details see @Log4jConfigurationHelper 
	 */
	public static void main(String[] args) throws InterruptedException{
		Log4jConfigurationHelper.init(Level.INFO) ;

		DataHunterLifecycleIteratorPvtScript thisTest = new DataHunterLifecycleIteratorPvtScript();

		//1: single
		thisTest.runSeleniumTest(KeepBrowserOpen.ONFAILURE);
		
		//2: multi-thread
//		thisTest.runMultiThreadedSeleniumTest(2, 2000);

		//3: multi-thread with parms
//		Map<String, java.util.List<String>>threadParameters = new java.util.LinkedHashMap<String,java.util.List<String>>();
//		threadParameters.put("USER",                              java.util.Arrays.asList( "USER-MATTHEW", "USER-MARK", "USER-LUKE", "USER-JOHN"));
//		threadParameters.put(SeleniumDriverFactory.HEADLESS_MODE, java.util.Arrays.asList( "true"        , "false"    , "true"     , "true"));		
//		thisTest.runMultiThreadedSeleniumTest(4, 2000, threadParameters);
	}
		
}
