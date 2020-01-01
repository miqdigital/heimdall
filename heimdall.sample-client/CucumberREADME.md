### Steps for using Heimdall with Cucumber framework:

- Add the maven dependency in the pom of the automation module:
 <dependency>
         <groupId>com.miq.automation</groupId>
         <artifactId>heimdall.application</artifactId>
         <version>${heimdall.application.version}</version>
  </dependency>

- Build the project. 
- Create a BOT or user in Slack.
- Generate the unique slack token. This is needed as to send the Slack notification from Heimdall.
- Create a Slack channel to receive build notifications of the projects.
- Create a properties file for the automation module. It should contain the following fields.
    
    1. HEIMDALL_BOT_TOKEN - the  the Slack token for BOT or user.
    2. CHANNEL_NAME - the channel for the build notifications.
    3. ISNOTIFYSLACK - this field can be true or false, accordingly.
    4. JIRA_PREFIX - the JIRA ticket prefix assigned to the team.
    5. S3_BUCKETNAME - the S3 bucket where the build results are pushed.
    
    An example of properties file is given under heimdall.sample-client/src/test/resources/properties, named runner.properties.

- The demo code is present in the CucumberTestRunner class in heimdall.sample-client module. Below is an explanation for the same.

        1. @RunWith(ExtendedCucumberRunner.class)
     Heimdall uses the cucumber-html-reports created after running the scenarios. ExtendedCucumber class helps to provide those reports.
    
        2. pathOfPropertyFile = "/path/to/properties/file"
     Contains the path to the properties file as a String. You can have different properties files for different environments.
    
        3. karateOutputPath = "target/cucumber-html-reports"
     Contains the path to the cucumber-html-reports, which are generated when test scenarios are run.
    
        4. Heimdall heimdall = new Heimdall(); 
     Generate an instance of the Heimdall class.
    
        5. heimdall.updateStatusInS3AndNotifySlack(pathOfPropertyFile, karateOutputPath)
     Call the updateStatusInS3AndNotifySlack with the Heimdall instance and pass
     pathOfPropertyFile and karateOutputPath as parameters. This method pushes the build
     information of tests to S3 and notifies the Slack channel of the same.
    
### Steps for writing scenarios:
    
- Create a test case ticket for the scenarios which will generate a unique ticket id, containing the JIRA prefix.
- Use this id as the Scenario Tag above the scenario description. For example - 
    
        @JIRA_PREFIX-4179 
        Scenario: This is a demo scenario.
    
- If the scenarios in the feature files don't contain tag ids, the report generated will not have the status of those test scenarios. 
- Every time the scenarios are run, always do - mvn clean test. ( This will clean up the target directory of the cucumber-html-reports ).
- If not done, the execution result of the previous test execution will also be included in the current test run.