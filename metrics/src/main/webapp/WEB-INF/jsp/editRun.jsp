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
<title>Mark59 - Application Run Editor</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>

</head>
<body>

 <%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 

 <h1>Edit Run - Comment and Baseline Detail</h1> 

<p>&nbsp;</p>
  

   <form:form method="post" action="updateRun" modelAttribute="run">
    <table>
     <tr>
      <td>RUN:  <br><br></td><td class=long> Application <b> ${run.application}</b> ,  Run Time <b> ${run.runTime} </b><br><br> </td>
     </tr>
     <tr>
      <td>Reference: </td>
      <td><form:input path="runReference" size="100"   />
      </td>
     </tr>        
     <tr>
      <td>Baseline? </td>
      <td><form:select path="baselineRun" items="${map.baselineRunYesNo}"  />
      </td>
     </tr>     
     <tr>
      <td>Comment:</td>
      <td><form:input  path="comment" size="100" height="20"  /> 
      </td>
     </tr>     
  
     <tr>
      <td> </td>
      <td><br><input type="submit" value="Save" />
      </td>
     </tr>
      <tr>
      <td colspan="2"><a href="runsList?reqApp=${map.reqApp}">Cancel</a></td>
     </tr>      
    </table>
    <form:hidden path="application" />
    <form:hidden path="runTime" />
    <form:hidden path="period" /> 
    <form:hidden path="duration" />    
   </form:form>

</div>

</body>
</html>