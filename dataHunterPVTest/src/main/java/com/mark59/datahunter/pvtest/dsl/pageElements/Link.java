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

package com.mark59.datahunter.pvtest.dsl.pageElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.mark59.selenium.utils.FluentWaitFactory;
import com.mark59.selenium.utils.SuperPageElement;

/**
 * Extending this Link class with @SuperPageElement is not necessary for the DataHunter application (due to its simple html).
 * It's just to provide and example of "wait" usage appropriate in more complex applications.
 * 
 * @author Philip Webb
 * Written: Australian Winter 2019
 */

@SuppressWarnings("rawtypes")
public class Link extends SuperPageElement  {

	/**
	 * Note as we are using a wait to find the link (fluentWait in the super class), the Link WebElement is not created 
	 * during instantiation of the Link object, as the link may not be rendered yet (the point of using a wait) 
	 */
	public Link(WebDriver driver, String linkText, boolean waitCondionsDebugMode) {
		super(driver, By.linkText(linkText), FluentWaitFactory.DEFAULT_TIMEOUT, FluentWaitFactory.DEFAULT_POLLING, waitCondionsDebugMode);
	}

	public Link(WebDriver driver, String linkText) {
		super(driver, By.linkText(linkText), FluentWaitFactory.DEFAULT_TIMEOUT, FluentWaitFactory.DEFAULT_POLLING, false);
	}

}
