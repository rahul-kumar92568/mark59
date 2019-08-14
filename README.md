# mark59

Mark59 is a Java-based framework that enables integration of Selenium scripts within the Jmeter Performance Test tool, also extending reporting capabilities.

## Early Release Notes (Aug 2019) 

The initial release presented the DataHunter application.  This application is designed to handle data retention and re-use between and during a performance test. 

The Mark59 core, selenium-implementation and sample dataHunterPVTest Java projects have now been included. Note at this point they have been added primarily for initial 'Beta' testing at iag as we produce the detailed documentation. The dataHunterPVTest project contains an example of a Jmeter/Selenium script using the mark59 framework. See the Mark59 Scripting Quickstart section below.

As the Mark59 core and selenium-implementation project artifacts are dependencies for scripting, they have been added to the Maven Central Repository (group id com.mark59).  To write a script using the lastest deploy to Central of Mark59 the dependency you need to add to your pom:   

	<dependency>
	  <groupId>com.mark59</groupId>
	  <artifactId>mark59-selenium-implementation</artifactId>
	  <version>1.0-beta-3</version>
	</dependency>


If you want to use our latest code base (ie, code checked in to this repo, but not yet deployed to the Central Repository), you need to: 
 
- (as a minium) Clone and Import the 'mark59-core' and 'mark59-selenium-implementation' maven projects from this repo into your Eclipse workspace

- Do a Maven Build on mark59-core using goals <b>clean package install</b>

- Do a Maven Build on mark59-selenium-implementation using goals <b>clean package install</b>

- The dependency you need to add to the pom of your Selenium Script project to point to these local builds is :   

		
		<dependency>
			<groupId>com.mark59</groupId>
			<artifactId>mark59-selenium-implementation</artifactId>
			<version>1.0-beta-4</version>
		</dependency>


Detailed documention is now being produced (high on our priority list over the coming weeks), however the source already contains detailed javadoc which should assist any 'early adopters'.   

## Releases

<b>1.0-beta-1 :</b> initial release

<b>1.0-beta-2 :</b> Maven Central release allows for CSV summary output files to be used in extended reporting (coming soon..)

<b>1.0-beta-3 :</b> Introduces an 'iterable' browser based class, w3c option set to false (v76 chromedriver/selenium compatabilty issues)

<b>1.0-beta-4 :</b> <b>IN PROGRESS</b> (not yet deployed to Central) Improvements in 'iterable' browser based class, improve javadocs..



## Terminologies

- DataHunter: The web-based application included in this git repository designed to handle performance test data.
- Selenium: Widely used Java-based browser automation tool.
- Jmeter: Leading Java-based performance test tool.
- Spring Boot: Java-based tooling which allows web-based applications to run without having to install a separate application server (such as Tomcat or WebSphere). 


## Key Concepts

- DataHunter is used as the 'sample application' for the install and testing of all other Mark59 artficats, as well as being useful in its own right.
- DataHunter can hold key-value pairs of data.
- Data can be held transiently (using a 'H2' database), or more permanently (using a MySQL database), depending on chosen implementation. 
- Both synchronous and asynchronous data life-cycles have been catered for.
- Page design has been kept minimal (no images or JavaScript on functional pages) as the application is designed for high volume usage in a performance test.
- DataHunter has been written as a Spring Boot application.  


## How DataHunter works

DataHunter is basically a set of simple data entry and usage screens.  As indicated above, the screens have deliberated kept minimal to maximise performance characteristics.

Here is a quick overview of DataHunter screen usage (labelled as per the main menu)

1. Home URL.

	The list of available pages.  This is the only page where images have been used (as this page is not expected to be hit during performance testing). 

2. add_policy.

	Key is Application, Identifier, Lifecycle.  We suggest generally 'Application' should identify a type of data, which can be keyed by 'Identifier'.  Lifecycle is an optional key element.
Useability can be REUSABLE (identifier can me selected multiple times), USED, UNUSED or  UNPAIRED (for asynchronous processing).
Other Data and Epoch Time can be optionally entered.

3. count_policies

	A unique count by Application, Lifecycle, Useability    

4. count_policies_breakdown_action

	A table of data pair counts (called 'policies' with the application). 

5. print_policy

	Displays data for a given policy

6. print_selected_policies

	Displays a list of polices matching the selection criteria.  Use this screen with caution in a performance test, as it can potentially bring back a large number of rows.   

7. delete_policy 

	Delete an Identifier for an Application

8. print_selected_policies

	Delete a set of polices matching the selection criteria.  NOTE with deletes no warnings or confirms are given.     

9. use_next_policy 

	Sets a policy to 'USED', as specified by the selection.   

10. lookup_next_policy 

	As per use_next_policy, but does not do an update (i.e., it lets you know what policy would be 'Used' next).   

11. update_policies_use_state

	Provides a mechanism to update a given Identifier (or an Application) with a chose Usage

12. async_message_analyzer

	Displays the state of asynchronous messages with the application. 

	Sample usage is provided in the dataHunterPVTest project, see method asyncLifeCycleTest() in the functional test script:
	
	    com.mark59.datahunter.pvtest.functionalTest.DataHunterSeleniumApiTest  

13. H2 Database Console 

	When using the H2 database, provides and SQL-style interface to the policy data. 


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

- Run!  You can see the progress in the View Results Tree listener.   Note: don't get stressed if you see some DataHunterLifeCycle results go red (failure).  The transaction 'DH-lifecycle-0299-sometimes-I-fail' may of been deliberately set to randomly fail a certai percentage of the time.

- Report generation coming soon.. 


