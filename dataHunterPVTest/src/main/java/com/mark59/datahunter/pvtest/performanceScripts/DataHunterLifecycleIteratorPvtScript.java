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
	private PolicySelectionCriteria policySelectionCriteria = new PolicySelectionCriteria();
	private DirectPageGets directPageGets;
	int forceTxnFailPercent;
	
	@Override
	protected Map<String, String> additionalTestParameters() {
		Map<String, String> jmeterAdditionalParameters = new LinkedHashMap<String, String>();
		
		jmeterAdditionalParameters.put(ITERATE_FOR_PERIOD_IN_SECS, 						"25");
		jmeterAdditionalParameters.put(ITERATE_FOR_NUMBER_OF_TIMES,  					"0");
		jmeterAdditionalParameters.put(ITERATION_PACING_IN_SECS,  						"10");
		jmeterAdditionalParameters.put(TestConstants.PARM_NAME_DATAHUNTER_URL_HOST_PORT,  DslConstants.DEFAULT_DATAHUNTER_URL_HOST_PORT);
		jmeterAdditionalParameters.put(TestConstants.PARM_NAME_DATAHUNTER_APPLICATION_ID, TestConstants.DATAHUNTER_PV_TEST);
		jmeterAdditionalParameters.put(TestConstants.PARM_NAME_FORCE_TXN_FAIL_PERCENT, 	"50");
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
		
		
		if (Thread.currentThread().getName().equals("DataHunterLifecycle 1-2")){
			System.out.println("SIMULATE FAILURE");
			throw new RuntimeException(" -- simulate failure -- ");
		}
		
		
// 		add some policies
		Policies policy = new Policies(application, null, lifecycle, DslConstants.UNUSED, "", 0L);
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
		
//		dummy transaction just to trest transaction failure behaviour 		
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
	 * A main method to assist with script testing outside Jmeter.
	 * For logging details see @Log4jConfigurationHelper 
	 */
	public static void main(String[] args) throws InterruptedException{
		Log4jConfigurationHelper.init(Level.INFO) ;

		DataHunterLifecycleIteratorPvtScript thisTest = new DataHunterLifecycleIteratorPvtScript();
		thisTest.runSeleniumTest(KeepBrowserOpen.ONFAILURE);
//		thisTest.runMultiThreadedSeleniumTest(3, 9000);
	}

		
}
