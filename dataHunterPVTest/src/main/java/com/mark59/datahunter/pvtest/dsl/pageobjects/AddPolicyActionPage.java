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
import org.openqa.selenium.WebDriver;

import com.mark59.datahunter.pvtest.dsl.pageElements.Link;
import com.mark59.datahunter.pvtest.dsl.pageElements.PageTextElement;


/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class AddPolicyActionPage extends SuperDataHunterActionPage {
	
	public PageTextElement application;	
	public PageTextElement identifier;	
	public PageTextElement lifecycle;	
	public PageTextElement useability;
	public PageTextElement otherdata;	
	public PageTextElement epochtime;
	
	public Link backLink;
	
	public AddPolicyActionPage( WebDriver driver) {
		super(driver);
		
		application		= new PageTextElement(driver, By.id("application")); 				
		identifier		= new PageTextElement(driver, By.id("identifier")); 			
		lifecycle		= new PageTextElement(driver, By.id("lifecycle")); 			
		useability		= new PageTextElement(driver, By.id("useability")); 	
		otherdata		= new PageTextElement(driver, By.id("otherdata")); 	
		epochtime		= new PageTextElement(driver, By.id("epochtime")); 			
		
		backLink 		= new  Link(driver, "Back");

	}
	

}
