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

package com.mark59.metrics.data.sla.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mark59.metrics.data.beans.Sla;
import com.mark59.metrics.sla.SlaUtilities;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class SlaDAOjdbcImpl implements SlaDAO {

	@Autowired
	private DataSource dataSource;

	
	public void insertData(Sla sla) {
		/* transaction name and id the same thing for now.. */
		String sql = "INSERT INTO sla "
				+ "(TXN_ID, SLA_APPLICATION_KEY, IS_TXN_IGNORED, SLA_90TH_RESPONSE, "
				+ "SLA_PASS_COUNT, SLA_PASS_COUNT_VARIANCE_PERCENT, SLA_FAIL_COUNT, SLA_FAIL_PERCENT, SLA_REF_URL) VALUES (?,?,?,?,?,?,?,?,?)";

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql,
				new Object[] { sla.getTxnId(), sla.getSlaApplicationKey(),  sla.getIsTxnIgnored(), sla.getSla90thResponse(),
						sla.getSlaPassCount(),sla.getSlaPassCountVariancePercent(), sla.getSlaFailCount(), sla.getSlaFailPercent(), sla.getSlaRefUrl() });
	}



	@Override
	public int bulkInsertOrUpdateApplication(String graphApplication,	Sla slaKeywithDefaultValues) { 

		String sql =  "SELECT  DISTINCT TXN_ID, TXN_PASS, RUN_TIME FROM TRANSACTION TX WHERE"
				+ " TX.APPLICATION =  '" + graphApplication + "' AND"
				+ " TX.TXN_TYPE =  'TRANSACTION'  and"
				+ " TX.RUN_TIME = ( select max(RUN_TIME) from RUNS where RUNS.APPLICATION =  '" + graphApplication + "' AND RUNS.BASELINE_RUN = 'Y' )";

		System.out.println("bulkInsertOrUpdateApplication sql: "  + sql );		
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		String passedSlaDefaultRefUrlText =  slaKeywithDefaultValues.getSlaRefUrl();
		
		int rowCount = rows.size();
		System.out.println("bulkInsertOrUpdateApplication : number of rows to process: "  + rowCount );
		
		for (Map<String, Object> row : rows) {
//			System.out.println("bulkInsertOrUpdateApplication: " + slaDefaults.getApplication() + ":" + (String)row.get("TXN_ID") + ":" + (Long)row.get("TXN_PASS") );
			Sla existingSla =  getSla(slaKeywithDefaultValues.getSlaApplicationKey(), (String)row.get("TXN_ID") , null); 
			
			if (existingSla == null ){  //sla application-transaction does not exist, so add it
				slaKeywithDefaultValues.setTxnId((String)row.get("TXN_ID"));
				slaKeywithDefaultValues.setSlaPassCount((Long)row.get("TXN_PASS"));
				slaKeywithDefaultValues.setSlaRefUrl( passedSlaDefaultRefUrlText + ", (base:" + (String)row.get("RUN_TIME") + ")"    );	
//				System.out.println( "slaDefaults url < " +  slaDefaults.getSlaRefUrl() + ", PassCounts:" + (String)row.get("RUN_TIME") + ">"    );
				
				insertData(slaKeywithDefaultValues);
				
			} else {  // update the Pass Count, and URL ref (removing any previous 'base' comment) for an existing transaction
				
				existingSla.setSlaPassCount((Long)row.get("TXN_PASS"));
				existingSla.setSlaRefUrl( existingSla.getSlaRefUrl().replaceAll(", \\(base:(.*)\\)" , "")  + ", (base:" + (String)row.get("RUN_TIME") + ")"    );
	
				sql = "UPDATE sla set SLA_PASS_COUNT = ?, SLA_REF_URL = ? where SLA_APPLICATION_KEY=? and TXN_ID = ?";
				jdbcTemplate = new JdbcTemplate(dataSource);
		
				jdbcTemplate.update(sql, new Object[] { existingSla.getSlaPassCount(), existingSla.getSlaRefUrl(), existingSla.getSlaApplicationKey(), existingSla.getTxnId() });
			}
		}
		return rowCount;
	}	
		
	

	@Override
	public void deleteAllSlasForApplication(String slaApplicationKey) {
		String sql = "delete from sla where  SLA_APPLICATION_KEY='" + slaApplicationKey + "'";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);

	}
	
	@Override
	public void deleteData(String slaApplicationKey, String txnId) {
		String sql = "delete from sla where  SLA_APPLICATION_KEY='" + slaApplicationKey + "' and TXN_ID='" + txnId + "'";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);

	}

	/*
	 * delete/insert (i.e. rename) if transaction does not exist within the given application,  otherwise update the new values for the passed transaction name
	 */
	@Override
	public void updateData(Sla sla) {

		Sla existingSla =  getSla(sla.getSlaApplicationKey(), sla.getTxnId() , null); 
//		System.out.println("SlaDAOjdbcImpl.updateData: app=" + sla.getApplication() + ", TxnId=" + sla.getTxnId() + ",  existingSla? = " + existingSla);	
		
		if (existingSla == null ){  //a transaction 'rename'
			deleteData(sla.getSlaApplicationKey(), sla.getSlaOriginalTxnId());
			insertData(sla);
			
		} else {  // update values for an existing transaction
				
			String sql = "UPDATE sla set SLA_APPLICATION_KEY = ?, IS_TXN_IGNORED = ?,SLA_90TH_RESPONSE = ?, "
					+ "SLA_PASS_COUNT = ?, SLA_PASS_COUNT_VARIANCE_PERCENT = ?, SLA_FAIL_COUNT = ?, SLA_FAIL_PERCENT = ?, SLA_REF_URL = ? "
					+ "where SLA_APPLICATION_KEY=? and TXN_ID = ?";
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
	
			jdbcTemplate.update(sql, new Object[] { sla.getSlaApplicationKey(), sla.getIsTxnIgnored(), sla.getSla90thResponse(),
					sla.getSlaPassCount(),sla.getSlaPassCountVariancePercent(), sla.getSlaFailCount(), sla.getSlaFailPercent(), sla.getSlaRefUrl(),	sla.getSlaApplicationKey(), sla.getTxnId(), });
		}
	}
	

	@Override
	public Sla getSla(String slaApplicationKey, String txnId, String defaultSlaForApplicationKey) {
		List<Sla> slaList = new ArrayList<Sla>();
		String sql = "select * from sla where SLA_APPLICATION_KEY='" + slaApplicationKey + "' and TXN_ID='" + txnId + "'";
//		System.out.println("getSla sql = " + sql);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		slaList = jdbcTemplate.query(sql, new SlaRowMapper());
		
		if (slaList.isEmpty() )
			
			// a SLA for the transaction passed does not have a SLA, so now we check if the application has a 'default SLA-' - the second parameter expected to be passed thru
			
			if (defaultSlaForApplicationKey == null)
				return null;
			else			
				return  getDefaultApplicationSla(slaApplicationKey, defaultSlaForApplicationKey );
		else
			return slaList.get(0);
	}
	
	
	
	
	public Sla getDefaultApplicationSla(String slaApplicationKey,String defaultSlaForApplicationKey) {
		List<Sla> slaList = new ArrayList<Sla>();
		String sql = "select * from sla where  SLA_APPLICATION_KEY='" + slaApplicationKey + "' and TXN_ID='" + defaultSlaForApplicationKey + "'";
//		System.out.println("getDefaultApplicationSla sql : " + sql);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		slaList = jdbcTemplate.query(sql, new SlaRowMapper());
		
		if (slaList.isEmpty()){
//			System.out.println("SlaDAOjdbcImpl.getDefaultApplicationSla is null!  (" + application + ":" + defaultApplicationSlaId + ")" );
			return null;
		} else {
//			System.out.println("SlaDAOjdbcImpl.getDefaultApplicationSla  (" + application + ":" + defaultApplicationSlaId + "), 90TH resp = " + slaList.get(0).getSla90thResponse() );
			return slaList.get(0);
		}	
	}
	
	
	
	@Override
	public List<Sla> getSlaList(String slaApplicationKey) {
		List<Sla> slaList = new ArrayList<Sla>();
		String sql = "select * from sla where SLA_APPLICATION_KEY='" + slaApplicationKey	+ "' order by TXN_ID"   ;
//		System.out.println("getSla sql = " + sql);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		slaList = jdbcTemplate.query(sql, new SlaRowMapper());
		return slaList;
	}
	
	public List<Sla> getSlaList() {
		List<Sla> slaList = new ArrayList<Sla>();
		String sql = "select * from sla order by SLA_APPLICATION_KEY, TXN_ID";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		slaList = jdbcTemplate.query(sql, new SlaRowMapper());
		return slaList;
	}


	@Override
	@SuppressWarnings("rawtypes")
	public List<String> findSlaApplicationKeys() {
		String sql = "SELECT distinct SLA_APPLICATION_KEY FROM sla order by SLA_APPLICATION_KEY";

		List<String> applications = new ArrayList<String>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		for (Map row : rows) {
			applications.add( (String)row.get("SLA_APPLICATION_KEY") );
//			System.out.println("populating application in dropdown list : " + row.get("APPLICATION")  ) ;
		}	
		return  applications;
	}


	
	/* 
	 * Reports on transactions which appear on the SLA table, but do not exist in the run, unless Sla's are switched off (ie all set to negative numbers)  
	 *           
	 * Transactions on the SLA table which have the 'Ignore on Graphs' flag set are also included in the check, although only reported if any SLA is actually set ( a count
	 * or response time other than -1 has been entered against it)           
	 */
	@Override
	@SuppressWarnings("rawtypes")	
	public List<String> getSlasWithMissingTxnsInThisRun(String application, String runTime) {
		
		String sql = "SELECT TXN_ID FROM sla "
					+ " where SLA_APPLICATION_KEY='" + application	+ "' " 
					+ "  and TXN_ID not in ( "
					+ "     SELECT TXN_ID FROM transaction"
					+ "     where APPLICATION = '" + application + "' "
					+ "       and RUN_TIME = '" + runTime + "' " 
					+ "       and TXN_TYPE = 'TRANSACTION' ) " 					
					+ "  and TXN_ID != '-" + application + "-DEFAULT-SLA-' "
					+ "  and (   IS_TXN_IGNORED != 'Y' "  
					+ "          or "
					+ "          ( IS_TXN_IGNORED = 'Y' "
					+ "            and (not SLA_90TH_RESPONSE < 0.0 or not SLA_PASS_COUNT < 0 or not SLA_FAIL_COUNT < 0 or not SLA_FAIL_PERCENT < 0.0 )"
					+ "          ) "
      				+ "      ) "    
					+ " order by TXN_ID";

//		System.out.println("getSlasWithMissingTxnsInThisRun sql : " + sql  );
		
		List<String> missingTransactions = new ArrayList<String>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		for (Map row : rows) {
			missingTransactions.add( (String)row.get("TXN_ID") );
//			System.out.println("getSlasWithMissingTxnsInThisRun : txn  " + (String)row.get("TXN_ID") ) ;
		}	
		return  missingTransactions;
	}



	@Override
	public List<String> getListOfIgnoredTransactionsSQL(String graphApplication) {
		String sql = SlaUtilities.listOfIgnoredTransactionsSQL(graphApplication); 
		List<String> ignoredTransactions = new ArrayList<String>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		for (Map<String, Object> row : rows) {
			ignoredTransactions.add( (String)row.get("TXN_ID") );
//			System.out.println("populating application in dropdown list : " + row.get("APPLICATION")  ) ;
		}	
		return  ignoredTransactions;
	}

}
