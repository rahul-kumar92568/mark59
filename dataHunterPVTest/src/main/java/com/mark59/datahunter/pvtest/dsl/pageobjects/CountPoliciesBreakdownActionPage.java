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

package com.mark59.datahunter.pvtest.dsl.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.mark59.datahunter.pvtest.dsl.pageElements.Link;
import com.mark59.datahunter.pvtest.dsl.pageElements.PageTextElement;


/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class CountPoliciesBreakdownActionPage extends SuperDataHunterActionPage {
	
	public Link backLink;

	//TODO: be nice to capture the printed TABLE rows (or a subset)   
	
	public CountPoliciesBreakdownActionPage( WebDriver driver) {
		super(driver);
		backLink 	= new  Link(driver, "Back");
	}

	
	public int getCountForBreakdown(String application, String lifecycle, String useability){
		PageTextElement countForBreakdownElement = null;
		try { 
			countForBreakdownElement =  new PageTextElement(driver, By.id((application + "_" + lifecycle + "_" + useability + "_count").replace(" ", "_")));
		} catch ( NoSuchElementException e) {
			return 0;
		}
		int count = new Integer(countForBreakdownElement.getText());
		return count;
		
	}
	
}
