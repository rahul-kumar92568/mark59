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

package com.mark59.selenium.corejmeterimpl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.selenium.drivers.SeleniumDriverFactory;


/**
 * Selenium flavoured extension of the Jmeter Java Sampler AbstractJavaSamplerClient.
 * 
 * <p>A core class of the mark59 Selenium implementation, and should be extended when  
 * creating Jmeter-ready selenium script. 
 * 
 * <p>Implementation of abstract method runSeleniumTest should contain the test, with
 * parameterisation handled by additionalTestParameters.  See the 'DataHunter' sample provided for
 * implementation details 
 *      
 * <p>Includes a number of standard parameters expected for a Selenium WebDriver.</p>
 *
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public abstract class SeleniumIteratorAbstractJavaSamplerClient  extends  SeleniumAbstractJavaSamplerClient {

	public static Logger LOG = Logger.getLogger(SeleniumIteratorAbstractJavaSamplerClient.class);
	public static final String ITERATE_FOR_PERIOD_IN_SECS 	= "ITERATE_FOR_PERIOD_IN_SECS";
	public static final String ITERATE_FOR_NUMBER_OF_TIMES 	= "ITERATE_FOR_NUMBER_OF_TIMES";
	public static final String ITERATION_PACING_IN_SECS 	= "ITERATION_PACING_IN_SECS";	
	public static final String STOP_THREAD_ON_FAILURE 		= "STOP_THREAD_ON_FAILURE";
	
	private KeepBrowserOpen keepBrowserOpen = KeepBrowserOpen.NEVER;

	
	private static final Map<String,String> defaultIterArgumentsMap;	
	static {
		Map<String,String> staticIterMap = new LinkedHashMap<String,String>();

		staticIterMap.put("______________________ interation settings: _____________________", "" );		
		staticIterMap.put(ITERATE_FOR_PERIOD_IN_SECS, 						"0");
		staticIterMap.put(ITERATE_FOR_NUMBER_OF_TIMES,  					"1");
		staticIterMap.put(ITERATION_PACING_IN_SECS,  						"0");
		staticIterMap.put(STOP_THREAD_ON_FAILURE,		  String.valueOf(false));
		staticIterMap.putAll(defaultArgumentsMap);
		
		defaultIterArgumentsMap = Collections.unmodifiableMap(staticIterMap);		
	}
	
	
	/** 
	 * Creates the list of parameters with default values, as they would appear on the Jmeter GUI for the JavaSampler being implemented.
	 * <p>A standard set of parameters are defined (defaultArgumentsMap). Additionally,an implementing class (the script extending this class) 
	 * can add additional parameters (or override the standard defaults) via the additionalTestParameters() method.    
	 * 
	 * @see #additionalTestParameters()
	 * @see org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient
	 */
	@Override
	public Arguments getDefaultParameters() {
		return Mark59Utils.mergeMapWithAnOverrideMap(defaultIterArgumentsMap, additionalTestParameters());
	}
	
		

	/**
	 * {@inheritDoc}
	 * 
	 *  Note the use of the catch on  AssertionError - as this is NOT an Exception but an Error, and therefore need to be explicitly caught. 
	 */
	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		if (LOG.isDebugEnabled()) LOG.debug(this.getClass().getName() +  " : exectuing runTest (iterator)" );

		if ( context.getJMeterContext() != null  && context.getJMeterContext().getThreadGroup() != null ) {
			tg     = context.getJMeterContext().getThreadGroup();
			tgName = tg.getName();
		}
		
		if (IpUtilities.localIPisNotOnListOfIPaddresses(context.getParameter(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST))){ 
			LOG.info("Thread Group " + tgName + " is stopping (not on 'Restrict to IP List')" );
			if (tg!=null) tg.stop();
			return null;
		}
	
		Map<String,String> jmeterRuntimeArgumentsMap = convertJmeterArgumentsToMap(context);
		
		seleniumDriverWrapper = new SeleniumDriverFactory().makeDriverWrapper(jmeterRuntimeArgumentsMap) ;

		driver =  (WebDriver)seleniumDriverWrapper.getDriverPackage() ;
		jm = new JmeterFunctionsForSeleniumScripts(thread, seleniumDriverWrapper, jmeterRuntimeArgumentsMap);   	
				
		try {
			LOG.debug(">> initiateSeleniumTest");			
			initiateSeleniumTest(context, jm, driver);
			LOG.debug("<< finished initiateSeleniumTest" );

			Long scriptStartTimeMs 		 = System.currentTimeMillis(); 			
			Long iterateForPeriodMs 	 = convertToLong(ITERATE_FOR_PERIOD_IN_SECS, context.getParameter(ITERATE_FOR_PERIOD_IN_SECS),0L) * 1000;
			Integer iterateNumberOfTimes = convertToInteger(context.getParameter(ITERATE_FOR_NUMBER_OF_TIMES));
			Long iterationPacingMs       = convertToLong(ITERATION_PACING_IN_SECS, context.getParameter(ITERATION_PACING_IN_SECS),0L) * 1000;
			long scriptIterationStartTimeMs;
			long delay;
			
			if (LOG.isDebugEnabled()) LOG.debug(thread + ": tgName = " + tgName + ", scriptStartTimeMs = " + scriptStartTimeMs + ", iteratePeriodMs = " + iterateForPeriodMs + ", iterateNumberOfTimes = " + iterateNumberOfTimes );
		
			if (iterateForPeriodMs==0 && iterateNumberOfTimes==0 ) {
				LOG.info("Thread Group " + tgName + " is stopping (neither ITERATE_FOR_PERIOD_IN_SECS or ITERATE_FOR_NUMBER_OF_TIMES have been set to a valid non-zero value)" );
				if (tg!=null) tg.stop();
				return null;
			}
			int i=0;
			
			while ( ! areIterateEndConditionsMet(scriptStartTimeMs, iterateForPeriodMs, iterateNumberOfTimes, i )) {
				i++;
				LOG.debug(">> iterateSeleniumTest (" + i + ")");
				scriptIterationStartTimeMs =  System.currentTimeMillis();
				
				iterateSeleniumTest(context, jm, driver);
				
				delay =	iterationPacingMs + scriptIterationStartTimeMs - System.currentTimeMillis();
			    if (delay < 0){
			         LOG.info("  script execution time exceeded pacing by  : " + (0-delay) + " ms."  );
			         delay = 0;
			    }
		        LOG.debug("<<  iterateSeleniumTest - script execution sleeping for : " + delay + " ms."  );
			    Thread.sleep(delay);
			}
			
			LOG.debug(">> running finalizeSeleniumTest ");			
			finalizeSeleniumTest(context, jm, driver);
			LOG.debug("<< finished finalizeSeleniumTest" );			
						
			jm.tearDown();

		} catch (Exception | AssertionError e) {

			scriptExceptionHandling(e);	

			if ("true".equalsIgnoreCase(context.getParameter(STOP_THREAD_ON_FAILURE))){
				LOG.info("Thread Group " + tgName + " is stopping (script failure, and STOP_THREAD_ON_FAILURE is set to true)" );
				context.getJMeterContext().getThreadGroup().stop();
			}
			
		} finally {
			if (! keepBrowserOpen.equals(KeepBrowserOpen.ALWAYS )     ) { 
				seleniumDriverWrapper.driverDispose();
			}
		}
		return jm.getMainResult();
	}


	private Long convertToLong(String parameterName, String parameter, Long returnedValueForInvalidParameter ) {
		Long convertedLong = returnedValueForInvalidParameter;
		if (parameter!= null) parameter = parameter.trim();
		if (NumberUtils.isCreatable(parameter)){
			convertedLong = Long.valueOf(parameter.trim());
		} else {
			LOG.info(returnedValueForInvalidParameter + " is being assumed for the paramter '" + parameterName + "'" );
		}
		return convertedLong;
	}

	private Integer convertToInteger(String parameter) {
		Integer convertedInt = 0;
		if (parameter!= null) parameter = parameter.trim();		
		if (StringUtils.isNumeric(parameter.trim())){
			convertedInt = Integer.valueOf(parameter);
		}
		return convertedInt;
	}

	private boolean areIterateEndConditionsMet(Long scriptStartTimeMs, Long iterateForPeriodMs, Integer iterateNumberOfTimes, Integer alreadyIterated) {
		if ( iterateNumberOfTimes > 0 && alreadyIterated >= iterateNumberOfTimes ) {
			return true;
		}
		if ( iterateForPeriodMs > 0 &&  System.currentTimeMillis() > scriptStartTimeMs + iterateForPeriodMs  ) {
			return true;
		}
		return false;
	}



	/**
	 * no implementation for a SeleniumIteratorAbstractJavaSamplerClient test
	 */
	@Override
	protected void runSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm, WebDriver driver) {}
	
	
	protected abstract void initiateSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm, WebDriver driver);
	protected abstract void iterateSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm, WebDriver driver);	
	protected abstract void finalizeSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm, WebDriver driver);	

}
