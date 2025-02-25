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

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class HtmlTableRow {

	private WebElement tableRow;
	
	public HtmlTableRow(WebElement tableRow){
		this.tableRow = tableRow;
	}

	
	public List<WebElement> getElementsForRow(){
		List<WebElement> tableRowElements = new ArrayList<>();
		tableRowElements = tableRow.findElements(By.tagName("td"));
		return tableRowElements;
	}
	
	
	public List<WebElement> getElementsForRow(int expectedNumberOfColumns){
		List<WebElement> tableRowElements = getElementsForRow();
		if (tableRowElements.size() != expectedNumberOfColumns ){
			throw new RuntimeException("printSelectedPolicies : unexpected number of cols.  Got" + tableRowElements.size() + ", expected " + expectedNumberOfColumns );
		}	
		return tableRowElements;
	}
	
}
