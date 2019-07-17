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

import org.openqa.selenium.WebDriver;

import com.mark59.datahunter.pvtest.dsl.pageElements.DropdownList;
import com.mark59.datahunter.pvtest.dsl.pageElements.InputTextElement;
import com.mark59.datahunter.pvtest.dsl.pageElements.SubmitBtn;


/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class AsyncMessageAnalyzerPage extends SuperDataHunterPage  {
	
	public  DropdownList applicationStartsWithOrEquals; 		
	public  InputTextElement application;
	public  InputTextElement identifier;		
	public  DropdownList useability;
	public  SubmitBtn submit;			

	
	public AsyncMessageAnalyzerPage( WebDriver driver) {
		super(driver);
		applicationStartsWithOrEquals  = new DropdownList(driver, "applicationStartsWithOrEquals");				
		application = new InputTextElement(driver, "application");
		identifier  = new InputTextElement(driver, "identifier");
		useability  = new DropdownList(driver, "useability");	
		submit   	= new SubmitBtn(driver, "submit");		
	}

	
}
