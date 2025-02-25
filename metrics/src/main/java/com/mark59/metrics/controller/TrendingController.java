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

package com.mark59.metrics.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mark59.metrics.application.AppConstants;
import com.mark59.metrics.application.Utils;
import com.mark59.metrics.data.application.dao.ApplicationDAO;
import com.mark59.metrics.data.beans.BarRange;
import com.mark59.metrics.data.beans.GraphMapping;
import com.mark59.metrics.data.beans.Run;
import com.mark59.metrics.data.beans.Transaction;
import com.mark59.metrics.data.graphMapping.dao.GraphMappingDAO;
import com.mark59.metrics.data.metricSla.dao.MetricSlaDAO;
import com.mark59.metrics.data.run.dao.RunDAO;
import com.mark59.metrics.data.sla.dao.SlaDAO;
import com.mark59.metrics.data.transaction.dao.TransactionDAO;
import com.mark59.metrics.form.TrendingForm;
import com.mark59.metrics.graphic.data.VisGraphicDataProduction;
import com.mark59.metrics.metricSla.MetricSlaChecker;
import com.mark59.metrics.metricSla.MetricSlaResult;
import com.mark59.metrics.metricSla.SlaResultTypeEnum;
import com.mark59.metrics.sla.SlaChecker;
import com.mark59.metrics.sla.SlaTransactionResult;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */

@Controller
public class TrendingController {
	
	@Autowired
	ApplicationDAO applicationDAO; 

	@Autowired
	RunDAO runDAO; 	
	
	@Autowired
	TransactionDAO transactionDAO;
	
	@Autowired
	SlaDAO slaDAO; 	

	@Autowired	
	MetricSlaDAO metricSlaDAO;
		
	@Autowired
	GraphMappingDAO graphMappingDAO; 	
	

	 //TODO: increase and rationalize input validation (messages, prevent fails etc).   
	
	@RequestMapping(value="/trending",  method = RequestMethod.GET )
	public String loadTrendingPage( @RequestParam(required=false) String reqApp,
									@RequestParam(required=false) String reqGraph,	
									@RequestParam(required=false) String reqSqlSelectLike,		
									@RequestParam(required=false) String reqSqlSelectNotLike,
									@RequestParam(required=false) String reqManuallySelectTxns,
									@RequestParam(required=false) String reqChosenTxns,	
									@RequestParam(required=false) String reqUseRawSQL,
									@RequestParam(required=false) String reqTransactionIdsSQL,
									@RequestParam(required=false) String reqNthRankedTxn,
									@RequestParam(required=false) String reqSqlSelectRunLike,		
									@RequestParam(required=false) String reqSqlSelectRunNotLike,
									@RequestParam(required=false) String reqManuallySelectRuns,
									@RequestParam(required=false) String reqChosenRuns,
									@RequestParam(required=false) String reqUseRawRunSQL,
									@RequestParam(required=false) String reqRunTimeSelectionSQL,									
									@RequestParam(required=false) String reqMaxRun,
									@RequestParam(required=false) String reqMaxBaselineRun,
									@RequestParam(required=false) String reqAppListSelector,
									@RequestParam(required=false) String reqDisplayPointText,
									@RequestParam(required=false) String reqDisplayBarRanges,
									Model model, HttpServletRequest request ) {

		TrendingForm trendingForm = new TrendingForm();
		
		if (reqApp == null ){
			// on initial entry, when no application request parameter has been sent, take the first "active" application 
			reqAppListSelector = AppConstants.ACTIVE; 
			
			if ( ! runDAO.findApplications(reqAppListSelector).isEmpty()){
				reqApp = (String)runDAO.findApplications(reqAppListSelector).get(0);
			} else {														 // if no active apps, just use any existing app
				reqAppListSelector = AppConstants.ALL; 
				reqApp = (String)runDAO.findApplications(reqAppListSelector).get(0);
			} 
			if (StringUtils.isBlank(reqApp)){ 
				throw new RuntimeException("Whoa !!  No Applications found " );
			}
		}
		trendingForm.setApplication(reqApp);
		
		trendingForm.setAppListSelector(AppConstants.ACTIVE );
		if ( Utils.defaultIfNull(reqAppListSelector,AppConstants.ACTIVE).equals(AppConstants.ALL)){
			trendingForm.setAppListSelector(AppConstants.ALL);
		}
		if ( ! "Y".equals( applicationDAO.findApplication(reqApp).getActive() )){
			trendingForm.setAppListSelector(AppConstants.ALL);
		}

		trendingForm.setGraph(Utils.defaultIfNull(reqGraph, AppConstants.TXN_90TH_GRAPH)); 		// when no metric passed, assume txn 90th percentile 
		GraphMapping graphMapping = graphMappingDAO.findGraphMapping(trendingForm.getGraph());
		
		trendingForm.setSqlSelectLike(Utils.defaultIfNull(reqSqlSelectLike, "%"));
		trendingForm.setSqlSelectNotLike(Utils.defaultIfNull(reqSqlSelectNotLike, ""));
			
		trendingForm.setManuallySelectTxns(false);
		if ( Utils.defaultIfNull(reqManuallySelectTxns,"false").equals("true")){
			trendingForm.setManuallySelectTxns(true);
		}
		
		trendingForm.setChosenTxns(Utils.defaultIfBlank(reqChosenTxns,""));		
		
		trendingForm.setSqlSelectRunLike(Utils.defaultIfNull(reqSqlSelectRunLike, "%"));
		trendingForm.setSqlSelectRunNotLike(Utils.defaultIfNull(reqSqlSelectRunNotLike, ""));		
		
		trendingForm.setManuallySelectRuns(false);
		if ( Utils.defaultIfNull(reqManuallySelectRuns,"false").equals("true")){
			trendingForm.setManuallySelectRuns(true);		
		}

		trendingForm.setChosenRuns(Utils.defaultIfBlank(reqChosenRuns,""));
				
		trendingForm.setDisplayPointText(true);
		if ( Utils.defaultIfNull(reqDisplayPointText,"true").equals("false")){
			trendingForm.setDisplayPointText(false);	
		}
		
		trendingForm.setDisplayBarRanges(true);
		if ( Utils.defaultIfNull(reqDisplayBarRanges,"true").equals("false")){
			trendingForm.setDisplayBarRanges(false);	
		}
				
		trendingForm.setUseRawRunSQL(false);
		if ( Utils.defaultIfNull(reqUseRawRunSQL,"false").equals("true")){
			trendingForm.setUseRawRunSQL(true);
		}
		
		trendingForm.setRunTimeSelectionSQL("");
		if (! Utils.defaultIfBlank(reqRunTimeSelectionSQL ,"").equals("")) {
			trendingForm.setRunTimeSelectionSQL(Utils.decodeBase64urlParam(reqRunTimeSelectionSQL));		
		}
		
		trendingForm.setUseRawSQL(false);
		if ( Utils.defaultIfNull(reqUseRawSQL,"false").equals("true")){
			trendingForm.setUseRawSQL(true);
		}
		
		trendingForm.setTransactionIdsSQL("");
		if (! Utils.defaultIfBlank(reqTransactionIdsSQL ,"").equals("")) {
			trendingForm.setTransactionIdsSQL(Utils.decodeBase64urlParam(reqTransactionIdsSQL));		
		}		
		
		trendingForm.setMaxRun(Utils.defaultIfBlank(reqMaxRun, AppConstants.DEFAULT_10));
		trendingForm.setMaxBaselineRun(Utils.defaultIfBlank(reqMaxBaselineRun, AppConstants.DEFAULT_01));
	
		trendingForm.setNthRankedTxn(Utils.defaultIfBlank(reqNthRankedTxn, AppConstants.ALL));
		
		System.out.println("TrendingController trendingForm : " + trendingForm  );
		
		String runDatesToGraphId = runDAO.determineRunDatesToGraph(	trendingForm.getApplication(),
																	trendingForm.getSqlSelectRunLike(),
																	trendingForm.getSqlSelectRunNotLike(),				
																	trendingForm.isManuallySelectRuns(),
																	trendingForm.getChosenRuns(),
																	trendingForm.isUseRawRunSQL(), 
																	trendingForm.getRunTimeSelectionSQL(),
																	trendingForm.getMaxRun(),	
																	trendingForm.getMaxBaselineRun());  
		model.addAttribute("runDatesToGraphId", runDatesToGraphId);		
		
		if ( ! runDatesToGraphId.isEmpty()){ 
			
			trendingForm.setChosenRuns(runDatesToGraphId);

			String latestRunTime = Utils.commaDelimStringToStringList(runDatesToGraphId).get(0);

			String labelRunShortDescriptionsId = populateLabelRunShortDescriptionsId(trendingForm.getApplication(), runDatesToGraphId );
			model.addAttribute("labelRunShortDescriptionsId", labelRunShortDescriptionsId);
		
			String labelRunDescriptionsId = populateLabelRunDescriptionsId(trendingForm.getApplication(), runDatesToGraphId );
			model.addAttribute("labelRunDescriptionsId", labelRunDescriptionsId);

			trendingForm.setTransactionIdsSQL( transactionDAO.transactionIdsSQL(trendingForm.getApplication(), 
																				trendingForm.getGraph(), 
																				trendingForm.getSqlSelectLike(),
																				trendingForm.getSqlSelectNotLike(),
																				trendingForm.isManuallySelectTxns(),
																				trendingForm.getChosenTxns(), 
																				trendingForm.getNthRankedTxn(), 
																				trendingForm.getChosenRuns(),
																				trendingForm.isUseRawSQL(), 
																				trendingForm.getTransactionIdsSQL()));
			
			List<String> txnsToGraphList =  transactionDAO.determineTransactionIdsToGraph(trendingForm.getTransactionIdsSQL() );
			String txnsToGraphId = Utils.stringListToCommaDelimString(txnsToGraphList);
			model.addAttribute("txnsToGraphId", txnsToGraphId);

			trendingForm.setChosenTxns(txnsToGraphId);			
			
			if ( AppConstants.MAPPED_DATA_TYPES.TRANSACTION.name().equals( graphMapping.getTxnType() )){
				populateFailedTransactionalSlaLists(trendingForm.getApplication(), latestRunTime, txnsToGraphId, model);
				populateIgnoredTransactionsList(trendingForm.getApplication(), model);	
			} else {
				populateFailedMetricSlaLists(trendingForm.getApplication(), latestRunTime, txnsToGraphId, model, graphMapping);	
			}
		
			String trxnIdsRangeBarId = populateRangeBarData(trendingForm.getApplication(), latestRunTime, graphMapping.getGraph());	
			model.addAttribute("trxnIdsRangeBarId", trxnIdsRangeBarId );
					
			if ( !trendingForm.isUseRawRunSQL()){
				trendingForm.setRunTimeSelectionSQL(runDAO.getRunTimeSelectionSQL(trendingForm.getApplication(), trendingForm.getSqlSelectRunLike(), trendingForm.getSqlSelectRunNotLike() ));
			}

			String csvTextarea = VisGraphicDataProduction.createCsvData(trendingForm.getApplication(),
																		trendingForm.getGraph(), 
																		graphMapping.getUomDescription(), 
																		runDatesToGraphId, 
																		txnsToGraphList, 
																		transactionDAO);
			model.addAttribute("csvTextarea", csvTextarea );
		}	
		
		model.addAttribute(trendingForm);
		
		List<String> appListSelectorList = new ArrayList<String>();
		appListSelectorList.add("Active");
		appListSelectorList.add("All");
		model.addAttribute("appListSelectors", appListSelectorList);
		
		List<String> applicationList = populateApplicationDropdown(trendingForm.getAppListSelector());
		model.addAttribute("applications", applicationList);
		
		List<String> graphsList = populateMetricsDropdown();
		model.addAttribute("graphs", graphsList);
		
		model.addAttribute("barRangeLegendId", graphMapping.getBarRangeLegend()) ; 		
		model.addAttribute("txnTypedId", 	   graphMapping.getTxnType()) ; 	

		return "trending";
	}

	
	@RequestMapping(value="/trending", method=RequestMethod.POST)
	public String processTrendingForm(@RequestParam(required=false)String reqApp, @ModelAttribute("trendingForm") TrendingForm trendingForm, Model model, HttpServletRequest request ) {
		throw new RuntimeException("The POST method has been removed.  All requests are now by GET, using URL parms (to allow copy paste)" );
	}

	
	@RequestMapping("/trendingAsyncPopulateApplicationList" )	
	public @ResponseBody String trendingAsyncPopulateApplicationList(@RequestParam(required=false) String reqAppListSelector ) {  
		List<String> applicationList = populateApplicationDropdown(reqAppListSelector); 
		return  Utils.stringListToCommaDelimString(applicationList);  	
	}

	
	private String populateLabelRunShortDescriptionsId(String application, String runDatesToGraphId) {

		List<String> runDatesToGraph = Utils.commaDelimStringToStringList(runDatesToGraphId);
		List<String> runShortDescriptionsList = new ArrayList<String>();
		String runReferenceLinkText;

		for (int i = 0; i < runDatesToGraph.size();  i++) {
//			System.out.println("TrendingController:populateLabelRunDescriptionsId: runTimes.size() : " + runTimes.size() + " runTimes.get(" + i + ") : " + runTimes.get(i) );
			Run run = runDAO.findRun(application, runDatesToGraph.get(i));
			StringBuilder runDescriptionsb   = new StringBuilder();

			runDescriptionsb.append(run.getRunTime().substring(0,4)).append(".");
			runDescriptionsb.append(run.getRunTime().substring(4,6)).append(".");
			runDescriptionsb.append(run.getRunTime().substring(6,8)).append("T");   
			runDescriptionsb.append(run.getRunTime().substring(8,10)).append(":");
			runDescriptionsb.append(run.getRunTime().substring(10,12)).append(" ") ;
			
			if ( "Y".equalsIgnoreCase(run.getBaselineRun())){
				runDescriptionsb.append(" BASELINE ");
			}			

			runReferenceLinkText = StringUtils.substringBetween(run.getRunReference(),"'>","</a");
			if (runReferenceLinkText == null ){
				runReferenceLinkText = run.getRunReference();
			}
			runDescriptionsb.append(runReferenceLinkText);
			
			if ( run.getComment() != null  &&  !run.getComment().isEmpty() ){
				if (run.getComment().length() > 20 ){
					runDescriptionsb.append(" " +  run.getComment().substring(0,20));
				} else {
					runDescriptionsb.append(" " +  run.getComment());
				}
			}
			runShortDescriptionsList.add(runDescriptionsb.toString() );
		}
		String labelRunShortDescriptionsId = Utils.stringListToCommaDelimString(runShortDescriptionsList);
//		System.out.println("trendingController:populateLabelRunDescriptionsId: " + labelRunDescriptionsId );
		return labelRunShortDescriptionsId;
	}
	

	
	private String populateLabelRunDescriptionsId(String application, String runDatesToGraphId) {

		List<String> runDatesToGraph = Utils.commaDelimStringToStringList(runDatesToGraphId);
		List<String> runDescriptionsList = new ArrayList<String>();

		for (int i = 0; i < runDatesToGraph.size();  i++) {
			// System.out.println("TrendingController:populateLabelRunDescriptionsId: runTimes.size() : " + runTimes.size() + " runTimes.get(" + i + ") : " + runTimes.get(i) );
			Run run = runDAO.findRun(application, runDatesToGraph.get(i));
			StringBuilder runDescriptionsb   = new StringBuilder();
			runDescriptionsb.append(run.getRunReference());
			
			if ( run.getComment() != null  &&  !run.getComment().isEmpty() ){
				if (run.getComment().length() > 20 ){
					runDescriptionsb.append("<br><br><div style='color:grey;'>" +  run.getComment().substring(0,20) + "..</div>" );
				} else {
					runDescriptionsb.append("<br><br><div style='color:grey;'>" +  run.getComment() + "</div>" );
				}
			}
			if ( "Y".equalsIgnoreCase(run.getBaselineRun())){
				runDescriptionsb.append("<br><div id='bot' style='color:orange;vertical-align:bottom'>*baseline</div>");
			}
			runDescriptionsList.add(runDescriptionsb.toString() );
		}
		String labelRunDescriptionsId = Utils.stringListToCommaDelimString(runDescriptionsList);
//		System.out.println("trendingController:populateLabelRunDescriptionsId: " + labelRunDescriptionsId );
		return labelRunDescriptionsId;
	}
	
	private String populateRangeBarData(String application, String latestRunTime, String graph) {
		String trxnIdsRangeBarId = "";
		List<BarRange> trxnIdsRangeBarData = graphMappingDAO.getTransactionRangeBarDataForGraph(application, latestRunTime, graph); 
		for (BarRange barRange : trxnIdsRangeBarData) {
			trxnIdsRangeBarId = trxnIdsRangeBarId + "\"" + barRange.getTxnId() + "\",\"" + barRange.getBarMin().toPlainString() +  "\",\"" + barRange.getBarMax().toPlainString() + "\"\n";
		}
		return trxnIdsRangeBarId;
	}	
	

	private void populateFailedTransactionalSlaLists(String application, String latestRunTime, String txnsToGraphId,   Model model ) {
 
		Transaction transaction = new Transaction(); 
		
		List<String> trxnIds  = Utils.commaDelimStringToStringList(txnsToGraphId);
		
		Collection<Transaction> transactions = new ArrayList<Transaction>();
		for (String txnId : trxnIds) {
			transaction = transactionDAO.getTransaction(application, AppConstants.MAPPED_DATA_TYPES.TRANSACTION.name(), latestRunTime, txnId);
			if (transaction != null) {
				transactions.add(transaction);
			}
		}
		
		List<SlaTransactionResult> slaTransactionResultList =  new SlaChecker().listTransactionsWithFailedSlas(application, transactions, slaDAO);
		
		List<String> trxnIdsWithAnyFailedSla = new ArrayList<String>();
		List<String> trxnIdsWithFailedSla90thResponse = new ArrayList<String>();
		List<String> trxnIdsWithFailedSlaFailPercent = new ArrayList<String>();
		List<String> trxnIdsWithFailedSlaPassCount = new ArrayList<String>();		

		for (SlaTransactionResult slaTransactionResult : slaTransactionResultList) {
			if ( !slaTransactionResult.isPassedAllSlas()){
//				System.out.println("populateTrxnIdsWithFailedSlaId : sla fail for " + slaTransactionResult.getTxnId() );
				trxnIdsWithAnyFailedSla.add(slaTransactionResult.getTxnId());
			}
			if ( !slaTransactionResult.isPassed90thResponse()){
				trxnIdsWithFailedSla90thResponse.add(slaTransactionResult.getTxnId());
			}
			if ( !slaTransactionResult.isPassedFailPercent()){
				trxnIdsWithFailedSlaFailPercent.add(slaTransactionResult.getTxnId());
			}
			if ( !slaTransactionResult.isPassedPassCount()){
				trxnIdsWithFailedSlaPassCount.add(slaTransactionResult.getTxnId());
			}			
		}
		
		
		List<String> missingTransactions  =  new SlaChecker().checkForMissingTransactionsWithDatabaseSLAs(application, latestRunTime, slaDAO  );
		for (String missingTnxId : missingTransactions) {
			trxnIdsWithAnyFailedSla.add(missingTnxId);
		}
		
		model.addAttribute("trxnIdsWithAnyFailedSlaId", Utils.stringListToCommaDelimString(trxnIdsWithAnyFailedSla)  );
		model.addAttribute("trxnIdsWithFailedSla90thResponseId", Utils.stringListToCommaDelimString(trxnIdsWithFailedSla90thResponse) );	
		model.addAttribute("trxnIdsWithFailedSlaFailPercentId", Utils.stringListToCommaDelimString(trxnIdsWithFailedSlaFailPercent) );
		model.addAttribute("trxnIdsWithFailedSlaPassCount", Utils.stringListToCommaDelimString(trxnIdsWithFailedSlaPassCount) );
		model.addAttribute("missingTransactionsId", Utils.stringListToCommaDelimString(missingTransactions) );				
		
//		System.out.println(" trxnIdsWithFailedSla90thResponseId: " + Utils.stringListToCommaDelimString(trxnIdsWithFailedSla90thResponse)   );
//		System.out.println(" trxnIdsWithFailedSlaFailPercentId: "  + Utils.stringListToCommaDelimString(trxnIdsWithFailedSlaFailPercent)   );
//		System.out.println(" trxnIdsWithFailedSlaPassCount: "      + Utils.stringListToCommaDelimString(trxnIdsWithFailedSlaPassCount)   );
	}	
	
	private void populateIgnoredTransactionsList(String application, Model model){
		List<String> ignoredTransactionsId = slaDAO.getListOfIgnoredTransactionsSQL(application);
		model.addAttribute("ignoredTransactionsId", Utils.stringListToCommaDelimString(ignoredTransactionsId) );		
	}
	
	private void populateFailedMetricSlaLists(String application, String latestRunTime, String txnsToGraphId,  Model model, GraphMapping graphMapping){
	
		String metricTxnType = graphMapping.getTxnType(); 
				
		List<MetricSlaResult> metricSlaResults  = new MetricSlaChecker().listFailedMetricSLAs(application, latestRunTime, metricTxnType, metricSlaDAO, transactionDAO);

		List<String> trxnIdsWithFailedSla = new ArrayList<String>();
		List<String> trxnIdsWithFailedSlaForThisMetricMeasure = new ArrayList<String>();
		
		List<String> missingSlaTransactions = new ArrayList<String>();
		
		/* if the graph being plotted is using the same transaction field (derivation) as the one the SLA failure if for, its name will be marked in red on the graph.  
		 * If the SLA is just for the same metric type, then the value only gets marked in red (just to indicate there is a problem with this value - but not for this graph) 
		 */
		String graphValueDerivation = graphMapping.getValueDerivation(); 
		
		for (MetricSlaResult metricSlaResult : metricSlaResults) {
			if ( ! metricSlaResult.getSlaResultType().equals(SlaResultTypeEnum.MISSING_SLA_TRANSACTION)){ 
//				System.out.println("populateFailedMetricSlaLists : sla fail for " + metricSlaResult.getTxnId() + ":"+metricSlaResult.getValueDerivation() + ":"+ graphValueDerivation );
				if (metricSlaResult.getValueDerivation().equalsIgnoreCase(graphValueDerivation)){
					trxnIdsWithFailedSla.add(metricSlaResult.getTxnId());
					trxnIdsWithFailedSlaForThisMetricMeasure.add(metricSlaResult.getTxnId());						
				} else {   
					assert metricTxnType.equalsIgnoreCase(metricSlaResult.getMetricTxnType());
					trxnIdsWithFailedSla.add(metricSlaResult.getTxnId());	
				}
		
			} else {   // Missing SLA (show only on graphs using the same Metric derivation as the Sla. eg derivation of 'Average' for a metric type of 'CPU_UTIL') 
				
				System.out.println("populateMissingMetricSlaLists : sla  " + metricSlaResult.getTxnId() + ":"+metricSlaResult.getValueDerivation() + ":"+ graphValueDerivation );				
				if (metricSlaResult.getValueDerivation().equalsIgnoreCase(graphValueDerivation)){
					System.out.println("so a missing sql exists for this graph !");	
					missingSlaTransactions.add(metricSlaResult.getTxnId());
					trxnIdsWithFailedSla.add(metricSlaResult.getTxnId());
				}
			}
		
		} 
		model.addAttribute("trxnIdsWithAnyFailedSlaId",					Utils.stringListToCommaDelimString(trxnIdsWithFailedSla));
		model.addAttribute("trxnIdsWithFailedSlaForThisMetricMeasure", 	Utils.stringListToCommaDelimString(trxnIdsWithFailedSlaForThisMetricMeasure));
		model.addAttribute("missingTransactionsId", Utils.stringListToCommaDelimString(missingSlaTransactions) );
	};	
	
	
	private List<String> populateApplicationDropdown(String appListSelector ) {
		List<String> applicationList = runDAO.findApplications(appListSelector);
		return applicationList;
	}		
	

	private List<String> populateMetricsDropdown() {
		List<String> metricsList = new ArrayList<String>();
		List<GraphMapping> graphsList = graphMappingDAO.getGraphMappings();
		for (GraphMapping graphMapping : graphsList) {
			metricsList.add( graphMapping.getGraph() );
		}
		return metricsList;
	}		
	
}
