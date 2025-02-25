<!-- Copyright 2019 Insurance Australia Group Limited
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. 
  
  Author:  Philip Webb
  Date:    Australian Winter 2019
  -->
  
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<!DOCTYPE html>
<html>
<head>
<title>Mark59 - Edit Metric SLA</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>

<body>

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 

 <h1>Edit Metric SLA Details</h1> 

<p>&nbsp;</p>
  

  <div>
   <form:form method="post" action="updateMetricSla?reqApp=${map.reqApp}" modelAttribute="metricSla">
    <table>
     <tr> <td>Application     :</td><td> ${map.metricSla.application} </td>   </tr>     
     <tr><td>Metric Name      :</td><td> ${map.metricSla.metricName} </td> </tr>
     <tr><td>Metric Type      :</td><td> ${map.metricSla.metricTxnType} </td> </tr>
     <tr><td>Value Derivation :</td><td> ${map.metricSla.valueDerivation} </td> </tr>

      
      <tr>
      <td>Sla Minimum Value:</td>
      <td><form:input path="slaMin" value="${map.metricSla.slaMin}" />
      </td>
     </tr>  
     <tr>    
       
     <tr>
      <td>Sla Maximum Value:</td>
      <td><form:input path="slaMax" value="${map.metricSla.slaMax}" />
      </td>
     </tr>  
     <tr>
      <td>Comment:</td>
      <td><form:input path="comment" value="${map.metricSla.comment}" />
      </td>
     </tr>       
   
     
     
     <tr>
      <td> </td>
      <td><input type="submit" value="Save" />
      </td>
     </tr>
     
     <tr>
      
      <td colspan="2"><a href="metricSlaList?reqApp=${map.reqApp}">Cancel</a></td>
     </tr>     
     
    </table>
    <form:hidden path="application" 		value="${map.metricSla.application}" />
    <form:hidden path="metricName" 			value="${map.metricSla.metricName}" />
    <form:hidden path="metricTxnType" 		value="${map.metricSla.metricTxnType}" />
    <form:hidden path="valueDerivation" 	value="${map.metricSla.valueDerivation}" />
    <form:hidden path="originalMetricName" 	value="${map.metricSla.originalMetricName}" />
   </form:form>
  </div>

</div>

</body>
</html>