# mark59

Mark59 is a Java-based framework that enables integration of Selenium scripts within the Jmeter Performance Test tool, also extending reporting capabilities.

## Early Release Notes (September 2019) 

The initial release presented the DataHunter application.  This application is designed to handle data retention and re-use between and during a performance test. 

All existing maven projects of Mark59 have now been included - core, selenium-implementation, mark59-server-metrics and sample dataHunterPVTest on the Jmeter side of things, metrics, metricsRuncheck and resultFilesConverter for reporting analysis.  Note at this point they have been added primarily for 'Beta' testing at iag as we produce the detailed documentation.  However, they are basically complete - unless a bug or high priority change comes in, we will drop the 'Beta' name from the next release :) 

As the Mark59 core and selenium-implementation project artefacts are dependencies for scripting, they have been added to the Maven Central Repository (group id com.mark59).  To write a script using the latest deploy to Central of Mark59 the dependency you need to add to your pom:   

	<dependency>
	  <groupId>com.mark59</groupId>
	  <artifactId>mark59-selenium-implementation</artifactId>
	  <version>1.0-beta-4</version>
	</dependency>


If you want to use our latest code base (ie, code checked in to this repo, but not yet deployed to the Central Repository), you need to: 
 
 <b> ***  Currently 'mark59-core' and 'mark59-selenium-implementation' are up to date on Central ***</b> 
 
- (as a minimum) Clone and Import the 'mark59-core' and 'mark59-selenium-implementation' maven projects from this repo into your Eclipse workspace

- Do a Maven Build on mark59-core using goals <b>clean package install</b>

- Do a Maven Build on mark59-selenium-implementation using goals <b>clean package install</b>

- The dependency you need to add to the pom of your Selenium Script project to point to these local builds is :   

		
		<dependency>
			<groupId>com.mark59</groupId>
			<artifactId>mark59-selenium-implementation</artifactId>
			<version>1.0-beta-4</version>
		</dependency>


Detailed documentation is now being produced (high on our priority list over the coming weeks), however the source already contains detailed javadoc which should assist any 'early adopters'.

Further down the track, we hope to separate out the Selenium 'DSL' from the DataHunterPVtest project into its own project, to give people a stand-alone "DSL-quick-start" option.

We also hope to provide a public AWS AMI, so we can easily demonstrate the entire CI flow of a performance testing using Mark59 components.

## Releases

<b>1.0-beta-1 :</b> initial release

<b>1.0-beta-2 :</b> Maven Central release allows for CSV summary output files to be used in extended reporting (coming soon..)

<b>1.0-beta-3 :</b> Introduces an 'iterable' browser based class, w3c option set to false (v76 chromedriver/selenium compatabilty issues)

<b>1.0-beta-4 :</b> Improvements in 'iterable' browser based class, allow for thread parameterization when running from Eclipse (both for 'iterable' and 'straight-through' scripts (see SeleniumAbstractJavaSamplerClient and SeleniumIteratorAbstractJavaSamplerClient).  Reporting projecs added. 



## Terminologies

- DataHunter: The web-based application included in this git repository designed to handle performance test data.
- Selenium: Widely used Java-based browser automation tool.
- Jmeter: Leading Java-based performance test tool.
- Spring Boot: Java-based tooling which allows web-based applications to run without having to install a separate application server (such as Tomcat or WebSphere). 


## The DataHunter Application 

- DataHunter is used as the 'sample application' for the install and testing of all other Mark59 artficats, as well as being useful in its own right.
- DataHunter can hold key-value pairs of data.
- Data can be held transiently (using a 'H2' database), or more permanently (using a MySQL database), depending on chosen implementation. 
- Both synchronous and asynchronous data life-cycles have been catered for.
- Page design has been kept minimal (no images or JavaScript on functional pages) as the application is designed for high volume usage in a performance test.
- DataHunter has been written as a Spring Boot application.  
DataHunter is basically a set of simple data entry and usage screens.  As indicated above, the screens have deliberated kept minimal to maximise performance characteristics.

Here is a quick overview of DataHunter screen usage (labelled as per the main menu)
1. Home URL. The list of available pages.  
2. add_policy.	Key is Application, Identifier, Lifecycle.  We suggest generally 'Application' should identify a type of data, which can be keyed by 'Identifier'.  Lifecycle is an optional key element. Useability can be REUSABLE (identifier can me selected multiple times), USED, UNUSED or  UNPAIRED (for asynchronous processing).
Other Data and Epoch Time can be optionally entered.
3. count_policies. A unique count by Application, Lifecycle, Useability    
4. count_policies_breakdown_action. A table of data pair counts (called 'policies' with the application). 
5. print_policy.  Displays data for a given policy
6. print_selected_policies. Displays a list of polices matching the selection criteria.
7. delete_policy. Delete an Identifier for an Application
8. print_selected_policies.Delete a set of polices matching the selection criteria.
9. use_next_policy. Sets a policy to 'USED', as specified by the selection.   
10. lookup_next_policy. As per use_next_policy, but does not do an update 
11. update_policies_use_state.	Provides a mechanism to update a given Identifier (or an Application) with a chose Usage
12. async_message_analyzer.	Displays the state of asynchronous messages with the application. Sample usage is provided in the dataHunterPVTest project, see method asyncLifeCycleTest() in the functional test script:
	
	    com.mark59.datahunter.pvtest.functionalTest.DataHunterSeleniumApiTest  

13. H2 Database Console. When using the H2 database, provides and SQL-style interface to the policy data. 


## DataHuter Quickstart

1. The following assumes you will be building the application in the Eclipse IDE, and have downloaded the repo on your Windows machine to C:\gitrepo\mark59.  You'll need to compensate within these instructions if you have done otherwise.     

2. Building the dataHunter application.
    1. In Eclipse, Import Existing Maven Project, from C:\gitrepo\mark59.  dataHunter/pom.xml should be the pom you need to select.
    2. Before you do the Maven build, check your Maven settings.xml file is correct (only applies to a corporate site where a Maven 'proxy' such a Nexus may be in use).
    3. Maven build with goals: clean package 
    4. To start the application in 'default' mode, follow the steps outlined in the file DataHunterStartFromMavenTarget.bat  (on the project root)
	
## Mark59 Scripting Quickstart

More detailed documentation to follow, however the basic idea can be seen using the dataHunterPVTest project. Windows is assumed, so you'll need to compensate for other systems:

- The sample test uses DataHunter, so you need to install DataHunter locally (see 'DataHunter Quickstart' above).

- Import dataHunterPVTest into Eclipse (as 'existing maven project') 


The 'mark59' sample script can be found at:

    com.mark59.datahunter.pvtest.performanceScripts.DataHunterLifecyclePvtScript
    
- Try to execute this script in Eclipse ('Run As - Java Application').  Even if the setup is all good, it is possible you will get a failure due to a mismatch of the Selenium Chromedriver and the version of Chrome on your machine.  If so, you can replace 'chromedriver.exe' in the project root with a compatible version (see http://chromedriver.chromium.org/downloads)


-(optional) Using the same steps as above, execute the 'iterator' sample script example at:

    com.mark59.datahunter.pvtest.performanceScripts.DataHunterLifecycleIteratorPvtScript
    

To run in Jmeter: 

- Build the project using Maven goals 'clean package'.  This will create a target jar of dataHunterPVTest.jar 

- (optional) Import and build project mark59-server-metrics.  This will create a target jar of mark59-server-metrics.jar

- Install a clean version of the current Jmeter release (5.1.1 at time of writing)

- copy mark59.properties and chromedriver.exe from the project root into the Jmeter /bin directory

- copy dataHunterPVTest.jar and (optional) mark59-server-metrics.jar into the Jmeter /lib/ext directory

- start Jmeter, and open test plan in the dataHunterPVTest project at \test-plans\DataHunterSeleniumApiTestPlan.jmx.  You will need to delete the ServerUtil_localhost Thread Group if you have not copied mark59-server-metrics.jar into lib/ext.

- Run!  You can see the progress in the View Results Tree listener.   Note: don't get stressed if you see some DataHunterLifeCycle results go red (failure).  The transaction 'DH-lifecycle-0299-sometimes-I-fail' may of been deliberately set to randomly fail a certain percentage of the time.


## Mark59 Reporting and Analysis

The results output file of a Jmeter run (can be xml or csv), is using by Mark59 in two flows:    

- to upload data to an analysis tool (metrics web application), where historical results, run comparisons and sla adherence can be viewed 

- to generate Jmeter report or reports 

Here are the key steps to getting each of these going.  We don't have any documentation yet for these projects, but hopefully you can get a feel of what's going on by getting them up and running. 

## Mark59 Analysis (Web) Tool Quickstart

Maven Project 'metrics' is the Analysis web application, project 'metricsRuncheck' provides the load program to get the data into the Analysis tool.   Start by building the 'metrics' Analysis web application.

- you will need to install MySql (we have built using verions 8.x 'community edition' of the product, so we suggest using a recent or the current version)

- during the MqSql install, the easiest thing to do is to take defaults (ie use port 3306), except at "MySQL" User Account, add User Name 'admin' password 'admin'  (you can also do it later if you miss it - see sample sql metrics\databaseScripts\MYSQLcreateAdminUser.sql)

- build the 'pvmetrics' database using the MySql Workbench 'import' panel on file metrics\databaseScripts\MYSQLpvmetricsDataBaseCreation.sql.  It contains sample runs for applications named 'DataHunter'  and 'DataHunterDistributed'.  The most recent 'DataHunter' run on the database links to sample report data embedded in the application, to give an indication of how we link to reports and CI runs.     

- build project 'metrics' using maven targets <b>clean package install</b>

- If you have the SpringBoot plugin install in eclipse, you can just start the app from the Boot Dashboard

- You can also start it from a CMD window.  Refer to metrics\MetricsStartFromMavenTarget.bat or \metrics\MetricsStartup.bat 

- URL is http://localhost:8080/metrics/ 

In order to load new test data into the tool, you need to build and execute metricsRuncheck.   A set of sample .bat files have been included in the projects, to assist in getting test results data loaded.  They use the 'DataHunter' applicaiton as the sample.

- build project 'metricsRuncheck' using maven targets <b>clean package</b>

- build project 'dataHunterPVTest' using maven targets <b>clean package</b>

- create a blank copy of Jmeter C:\apache-jmeter (all on your local machine is assumed)

- refer to and execute dataHunterPVTest\DataHunterDeployFromMavenTargetToJmeterInstance.bat, which will setup the Jmeter instance ready to run a DataHunter Jmeter test.

- run the Jmeter test plan at dataHunterPVTest\test-plans\DataHunterSeleniumApiTestPlan.jmx.  I suggest you run the plan from the Jmeter GUI in the first instance, to get an idea what's going on and to see errors more easily.  You can also try to run the test using bat file dataHunterPVTest\DataHunterExecuteJmeterTest.bat.

- refer to and execute \metricsRuncheck\LoadDataHunterTestResultsToMark59AnalysisDB.bat to load the run into the Analysis Tool 


## Mark59 Jmeter Report Generation Quickstart

- build project 'resultFilesConverter' using maven targets <b>clean package</b>

- refer to and execute resultFilesConverter\CreateDataHunterJmeterReports.bat to create the Jmeter Reports for the run (the bat file is set by default to split the report up by Transactions, CPU, Memory and Datapoints).


