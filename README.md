# Heimdall

## Test Reporting Tool

Heimdall is a Test Reporting Tool which is primarily built to publish your test execution results to the google sheets from
 
    a.Server(like Jenkins) 
    b.Local 
  
and notifies the recipients through the Communication channels (like Slack). This tool helps the recipients/users to know the current status of their products by executing the test cases just on a button click. Helps the recipients/users to make decisions for release instantaneously  and brings the transparency between your product and it's state.

### **Requirements**
1.Testcases ticket in Jira (we don’t support any other tool as of now)

2.Mobility Stream jira plugin (Helps to  Export Test cases to a Google Sheet and  maintain Realtime sync with Jira. You can use jira cloud plugin as well )

3.An Exported Test Cases Google sheet from the Mobility Stream Plugin (Acts as a source of Test Cases)

4.An Imported Google sheet (using Importrange) from sheet Generated in step(3) (Acts as a source to operate Heimdall )

5.Amazon s3 Account (Push the Test Execution Results)

6.Jenkins Job (Execute Test cases in Real Time)

7.Maven Automation projects (like cucumber/Karate for UI/API)

8.Tagged scenario's with feature files (Helps to Generate Reports at Tag level)

## **Steps to Set-up**
### **Phase -1 : Setting Up Test Cases From Jira to Google Spreadsheet**
1.Open Google spreadsheets with your Google Account

2.Click on Blank Page→ Add-ons→ Get add-ons→ Search for MobilityStream Jira Integration Plug-in and install
![Cannot load Image](images/MobilityStream.png)

3.Click on Add-ons→ MobilityStream Jira Integration(Hover on it)→ Click on settings.

     a.Input Jira URL: https://yourcompany.atlassian.net
     b.Input Username: Your Company's google account.
     c.Generate API Token Using this link : https://id.atlassian.com/manage/api-tokens
     d.Input the API Token got in the previous step and click on save.  
4.![Cannot load Image](images/MobilityStreamSaveInfo.png)

5.Open your Jira Dashboard → Click on Issues and Filters→ Click on Advanced search → Select type as a Test case


6.![](images/AdvancedSearch.png)


7.![](images/ExportJira.png)

8.Click on Google sheets Export on the top right corner( If Pop-up is blocked open the pop-up)

9.Select the Following Columns
##### 10.Click on the folder(Bottom left corner)→ Select Your Drive Folder→ Input Sheet name as (Team-name)-Testcase→ Input Update schedule as Real Time
11.![](images/SelectColumn.png)12.A sheet will be Generated which has the corresponding columns as selected in the above figure
##### 13.Create another blank Google sheet and import the Google sheet that is Generated in the step(8)   Example: =IMPORTRANGE("Google sheet link (created in step 8)","Data!A:M") Here A and M refers to the first and last column index of the google sheet created in step (11)","Data!A:M") Here A and M refers to the first and last column index of the google sheet created in step (8) 

### **Phase -2 : Setting Up Jenkins Job Configurations and Heimdall Add-on**
Create a jenkins job with the same name as your google sheet name (Created in Step 9 of Steps-to-set-up phase) which acts as source to execute heimdall (for example: project-tmt-test is the sheet name as well as the jenkins job name ).
![](images/TMTSheet.png)


![](images/jenkinsJob.png)

Note : User can Have multiple sheets, Jenkins job and job name as per the sheet and could be able to execute the corresponding jenkins gets triggered and Gets Google sheet published.

![](images/MultipleService.png)

#### **Setting-up Add-on for the Imported Google Sheet**
Click on the link : https://chrome.google.com/webstore/detail/heimdal/nicbooiejimgejoiccgbnibcaaocaibn?utm_source=permalink and click on the free option on the upper right corner of the

![](images/HeimdallAdd-on.png)

##### **To Get Jenkins Token :**
Open your jenkins →click on your profile→Configure→Add New Token→Copy the Token

![](images/JenkinsConfigure.png)

![](images/JenkinsAddToken.png)

#### **To configure the heimdall Add-On in the imported google sheet :**
1.Select the Add-ons menu from the google sheet →heimdall→Configurations

2.Provide the jenkins Token name as your mail-id

3.Provide the jenkins Token-id (Obtained Above)

4.Provide the jenkins domain name as "your Jenkins Home Page URL"

5.Provide the jenkins Job category

6.Provide s3 Access key

7.Provide s3 secret key

8.Provide the email id (of the team or individual mail-id) and slack webhook url(preferably teams' slack channel url) to receive the notifications of each build

9.Click on save, a pop-up box will be displayed mentioning that the configuration values are set

![](images/SaveUserProperties.png)

#### Heimdall Add-on Features

![](images/Heimdall.png)

###### **Test Executions**

![](https://media.giphy.com/media/MEjGD0cgUIKNUZkSDk/giphy.gif)

An alert message will pop-up saying Jenkins Job Triggered and you can see that jenkins will be triggered. If there is an error in between the flow, an alert message will pop-up with the corresponding message populated.
Note : Main Cause for error could be the Wrong Configurations provide by User in Heimdall.

###### **Update Build Info**

1.Click on Add-ons→Heimdall(Hover on it)→ Click on Update Build Info.
Google sheet will be updated with the Build data (The data for the builds which are not available previously in the sheet will be updated)

    Note: 
     Make sure you have the Build Information Stored in your Amazon s3 location for each build.
     Build Passed/Failed : 
     When there is no build running in jenkins and you select update build info  All the information of the latest builds ( builds previously not present in the sheet) gets updated
     Build is in Progress
     When the execution of jenkins job is still in progress. 
     All the build info until the latest executing build gets updated and you will be notified that the job for the latest build is still in progress. (Ex. Your sheet has info till build 34 and you  trigger build 37 . The data of build 35 and 36 gets updated in the sheet and the message as shown below gets displayed for build 37)

![](images/RunningJobAlert.png)

#### Colour Format and the Comments
Case 
1. Build number with Green color : If all the test cases gets passed then the build number cell on the top will be set to green color.

2.Build number with Pink color : If the number of failed test cases are greater than the passed test cases , the build number cell will be set to pink color.

3.Build number with Red color : If all the test cases gets failed the build number cell will be set to red color.

4.Build number with Yellow color : If the number of passed test cases are greater than the passed test cases , the build number cell will be set to yellow color.

5.Build number with no color : The cell will have no color when there is no test result in S3.  

6.When the sheet is updated with all the recent build information : When the sheet has all the recent build data and if updateBuild Info option is selected, alert message  is displayed informing that the sheet is up-to date.

![](images/SheetUpToDate.png)

#### Comments
Comment appears on each Field where User can see the corresponding Details of their corresponding Build information, where each passed /failed status shown provides the feature details, numbers of steps executed and the number of duration taken to execute the Test case.
![](images/Comment.png)