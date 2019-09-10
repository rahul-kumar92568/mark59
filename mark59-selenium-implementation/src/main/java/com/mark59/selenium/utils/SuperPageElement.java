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

package com.mark59.selenium.utils;

import java.time.Duration;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Useful class that implements a number of commonly used interactions with Selenium WebElement objects
 *
 * @author Michael Cohen
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public abstract class SuperPageElement<T extends SuperPageElement<?>> {

	private static final Logger LOG = Logger.getLogger(SuperPageElement.class);	
	
	protected WebDriver driver;
	protected By by;
	protected Duration timeout;
	protected Duration pollingFrequency;
	protected boolean waitCondionsDebugMode;

	
	public SuperPageElement(WebDriver driver, By by, Duration timeout, Duration pollingFrequency) {
		this(driver, by, timeout, pollingFrequency, false); 
	}

	public SuperPageElement(WebDriver driver, By by, Duration timeout, Duration pollingFrequency, boolean waitCondionsDebugMode) {
		this.driver = driver;
		this.by = by;
		this.timeout = timeout;
		this.pollingFrequency = pollingFrequency;
		this.waitCondionsDebugMode = waitCondionsDebugMode;
	}

	
	public WebElement waitForAndFindElement() {
		return waitUntilCondition(ExpectedConditions.elementToBeClickable(by));
	}
	

	@SuppressWarnings("unchecked")
	public T click() {
		waitForAndFindElement().click();
		return (T) this;
	}
	
	@SuppressWarnings("unchecked")
	public T clear() {
		waitForAndFindElement().clear();
		return (T) this;
	}
	
	public T waitUntillStale() {
		return waitUntillStaleBy(by);
	}
	
	@SuppressWarnings("unchecked")
	public T waitUntillStaleBy(By otherBy) {
		waitUntilCondition(ExpectedConditions.stalenessOf(driver.findElement(otherBy)));
		return (T) this;
	}
	
	/**
	 * Waits until the condition 'this element is clickable' is met.
	 * @return waitUntilClickableBy
	 */
	public T waitUntilClickable() {
		return waitUntilClickableBy(by);
	}
	
	@SuppressWarnings("unchecked")
	public T waitUntilClickableBy(By otherBy) {
		waitUntilCondition(ExpectedConditions.elementToBeClickable(otherBy));
		return (T) this;
	}
	
	
	@SuppressWarnings("unchecked")
	public T waitUntilTextPresentInElement(By otherBy, String expectedText) {
		waitUntilCondition(ExpectedConditions.textToBePresentInElementLocated(otherBy, expectedText));
		return (T) this;
	}

	
	
	public <C> C waitUntilCondition(ExpectedCondition<C> condition, boolean debug, Level level) {
		if (!debug) {
			return FluentWaitFactory.getFluentWait(driver, timeout, pollingFrequency).until(condition);
		} else {
			return waitUntilConditonsDebugMode(condition, level);  
		}
	}
	
	public <C> C waitUntilCondition(ExpectedCondition<C> condition, boolean debug) {
		if (!debug) {
			return FluentWaitFactory.getFluentWait(driver, timeout, pollingFrequency).until(condition);
		} else {
			return waitUntilConditonsDebugMode(condition);  
		}
	}
		
	public <C> C waitUntilCondition(ExpectedCondition<C> condition) {
		if (!waitCondionsDebugMode) {
			return FluentWaitFactory.getFluentWait(driver, timeout, pollingFrequency).until(condition);
		} else {
			return waitUntilConditonsDebugMode(condition);  
		}
	}

	private <C> C waitUntilConditonsDebugMode(ExpectedCondition<C> condition) {
		return waitUntilConditonsDebugMode(condition, Level.INFO);
	}
	
	private <C> C waitUntilConditonsDebugMode(ExpectedCondition<C> condition, Level level) {
		
		int webdriverTimeOutInSeconds = new Long( pollingFrequency.getSeconds() ).intValue() + 1;          //will generally get set to 1 
		
		int numberOfAttempts = new Long( timeout.getSeconds() / webdriverTimeOutInSeconds ).intValue();  
		
		for (int i = 1; i <= numberOfAttempts; i++ ) {
			try {
//				if (i>1) LOG.debug( "re-attempt (try #" + i + " of " + numberOfAttempts + ") timeout : " + webdriverTimeOutInSeconds + "s" );
				LOG.log(level, "re-attempt (try #" + i + " of " + numberOfAttempts + ") timeout : " + webdriverTimeOutInSeconds + "s" );
				return new WebDriverWait(driver, webdriverTimeOutInSeconds).until(condition);
			} catch (Exception e) {
				LOG.log(level, " condition for " + condition.getClass() + " not met : " + e.getMessage() );
			}
		}  
		
		throw new RuntimeException("** Exhausted all attempts - forcing a failure. ");
	}

	
	public By getBy() {
		return by;
	}

}
