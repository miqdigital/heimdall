var jenkinsJobName = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet().getName();

/**
 *  Runs the Jenkins job with the parameters specified
 *  @params {env} Environment parameter for Jenkins job
 *  @params {tags} Test type tag parameter for Jenkins job
 **/
function runJenkinsParaJob(env, tags) {
    Logger.log("Environment: " + env);
    Logger.log("Test Tag: " + tags);

    var jenkinValues = getJenkinsValues();
    Logger.log("Jenkins Value : " + jenkinValues);
    jenkinValues[2].method = 'post';
 
    try {
        var jenkinsURL = jenkinValues[0] + "/view/" + jenkinValues[1] + "/job/" + jenkinsJobName;
        var urlfetch = UrlFetchApp.fetch(jenkinsURL + "/buildWithParameters?environment=" + env + "&tags=" + tags, jenkinValues[2]);
        var url_to_getBuildNumber = urlfetch.getHeaders().Location + "/api/json";
        if (url_to_getBuildNumber != "") {
            var response = SpreadsheetApp.getUi().alert('Jenkins Job Triggered');
        } else {
            Logger.log("Error while triggering Jenkins Job");
        }
    } catch (e) {
        SpreadsheetApp.getUi().alert('Error while triggering Jenkins Job. Please set correct Jenkins Configuration values');
        Logger.log("Error in build details:" + e);
    }
}

/**
 *  Updates the build details to the spreadsheet
 *  @params {buildno} Build number which is not present in spreadsheet
 *  @params {buildData} Build informations
 **/
function updateBuildInfoToSheets(buildno, buildData) {
    var allRows = [];
    var data = JSON.parse(buildData);

    var environment = data.environment;
    var testType = data.testType;
    var dateTime = data.dateTime;
    var result = data.scenarioInfoList;
    var totalTests = data.totalTests;
    var passTestCount = data.passTestCount;
    var failTestCount = data.failTestCount;

    Logger.log("Environment : " + environment);
    Logger.log("TestType : " + testType);
    Logger.log("Date : " + dateTime);
    var buildNote = "Environment : " + environment + "\nTestType : " + testType.replace(new RegExp("@", 'g'), "") + "\nDateStamp : " + dateTime;
    buildNote = buildNote + "\nTotal Tests : " + totalTests + "\nTests passed : " + passTestCount + "\nTests failed : " + failTestCount;
    var mainSheet = SpreadsheetApp.getActiveSheet();
    var buildInfo = mainSheet.insertColumnAfter(Status_COL + 1).getRange(1, Status_COL + 2, 1)
        .setValue("BUILD-" + buildno).setNote(buildNote);

    mainSheet.getRange(2, 1, mainSheet.getLastRow()).getValues().map(function (arrayEle) {
        allRows.push("@" + arrayEle[0]);
    });

    if (totalTests == failTestCount) {
        buildInfo.setBackground(RED_COLOR);
    } else if (totalTests == passTestCount) {
        buildInfo.setBackground(GREEN_COLOR);
    } else if (failTestCount > passTestCount) {
        buildInfo.setBackground(LIGHTER_RED);
    } else {
        buildInfo.setBackground(YELLOW_COLOR);
    }

    for (var i in result) {
        var buildKey = [];
        var buildValue = [];
        var status = result[i].scenarioStatus;
        var issueType = result[i].scenarioTagId;

        var feature = result[i].featureDescription;
        var duration = result[i].scenarioTotalDuration;
        var steps = result[i].scenarioTotalSteps;
        buildKey.push("Feature", "Step duration", "Total steps")
        buildValue.push(feature, duration, steps)

        var note = "";
        if (allRows.indexOf(issueType) > -1) {
            mainSheet.getRange(allRows.indexOf(issueType) + 2, Status_COL + 2, 1).setValue(status);
            for (var size in buildKey) {
                note = note + buildKey[size] + " : " + buildValue[size] + "\n";
            }
            if (status == failedStatus) {
                mainSheet.getRange(allRows.indexOf(issueType) + 2, Status_COL + 2, 1).setValue(status).setFontColor(RED_COLOR);
                var failKey = [];
                var failStatus = [];
                var errMessage = result[i].scenarioStep.errMessage;
                var stepDuration = result[i].scenarioStep.stepDuration;
                var scenarioLine = result[i].scenarioStep.scenarioLine;
                failKey.push("Error", "Duration", "Scenario Line")
                failStatus.push(errMessage, stepDuration, scenarioLine)
                for (var size in failKey) {
                    note = note + failKey[size] + " : " + failStatus[size] + "\n";
                }
            }
            mainSheet.getRange(allRows.indexOf(issueType) + 2, Status_COL + 2, 1).setNote(note);
        }
    }
}

/**
 *  Retrieves all the build details of the project in firebase database
 **/
function getAllBuildCollection() {
    var buildnumbers = [];
    var buildnumber = [];
    var jenkinValues = getJenkinsValues();

    try {
        var newUrl = UrlFetchApp.fetch(jenkinValues[0] + "/view/" + jenkinValues[1] + "/job/" + jenkinsJobName + "/api/json", jenkinValues[2]);
        var buildData = JSON.parse(newUrl);
        for (var id in buildData.builds) {
            buildnumbers[id] = buildData.builds[id].number;
        }
        var count = 0;
        for (var id = buildnumbers.length - 1; id >= 0; id--) {
            var build_no = buildnumbers[id];
            var buildNum = build_no.toFixed(0);

            if (id == 0) {
                var urlForLatestBuild = UrlFetchApp.fetch(jenkinValues[0] + "/view/" + jenkinValues[1] + "/job/" + jenkinsJobName + "/lastBuild/api/json", jenkinValues[2]);
                var latestBuildData = JSON.parse(urlForLatestBuild);

                if (latestBuildData.result == null) {
                    var response = SpreadsheetApp.getUi().alert('Jenkins Job for BuildNumber ' + build_no + ' is in progress');
                    return;
                }
            }

            if (buildsAlreadyPresentInSheet.length >= 0) {
                if (buildsAlreadyPresentInSheet.indexOf(buildNum) == -1) {
                    getStatusFromS3(buildNum);
                    count = count + 1;
                }
            }
        }
        if (count == 0) {
            SpreadsheetApp.getUi().alert('Sheet is UpToDate');
        }
    } catch(e){
      Logger.log("Error in updating sheet " + e);
    }
}

/**
 *  Gets executed test data from S3
 *  @params {buildNumber} Jenkins build number to fetch the data from S3
 **/
//TODO: Provide IAM role implementation
function getStatusFromS3(buildNumber) {
    Logger.log("get status from s3")
    var bucketName = "miq-qa-test-status";
    var jenkinsDomain = userProperties.getProperty("jenkinsDomain");
    var url = jenkinsDomain + "/job/" + jenkinsJobName + "/" + buildNumber + "/console";
    var note = "Test Results not found in S3";
    var S3accessKeyId = userProperties.getProperty("s3AccessKey");
    var S3secretAccessKey = userProperties.getProperty("s3SecretKey");
    if (!configurationValidate(S3accessKeyId, S3secretAccessKey)) {
        SpreadsheetApp.getUi().alert('Please set S3 Access and Secret Key in Addon Configuration');
    } else {
        var service = new S3(S3accessKeyId, S3secretAccessKey);
        var objectName = jenkinsJobName + "/" + buildNumber + ".json";
        var blobObjectName = "blobtest";
        var options = {};

        try {
            var r = service.getObject(bucketName, objectName, options);

            if (r != null) {
                updateBuildInfoToSheets(buildNumber, r);
            } else {
                Logger.log("Error in fetching details from S3" + r);
                var buildInfo = mainSheet.insertColumnAfter(Status_COL + 1).getRange(1, Status_COL + 2, 1)
                    .setNote(note).setFormula('=HYPERLINK("' + url + '","BUILD-' + buildNumber + '")').setBackground(WHITE_COLOR);;
            }

        } catch (e) {
            Logger.log("Error in s3 trigger: " + e)
            throw new Error("Error in s3 trigger: " + e)
        }
    }

}


/**
 *  Validates the tags for null, undefined and empty values
 *  @params {firstTag} Tag value
 *  @params {secondTag} Tag value
 **/
function configurationValidate(firstTag, secondTag) {
    if (!firstTag || !secondTag || firstTag=="undefined" || secondTag=="undefined") {
        return false;
    }
    return true;
}

/**
 *  Gets the jenkins parameters from addon configuration
 *  @returns array containing JenkinsDomain, JenkinsJobCategory and option
 **/
function getJenkinsValues() {
    var id = userProperties.getProperty("tokenId");
    var name = userProperties.getProperty("tokenName");

    if (!configurationValidate(id, name)) {
        SpreadsheetApp.getUi().alert('Please set Jenkins Token and Name in Addon Configuration');
    } else {

        var header = {
            'Authorization': 'Basic ' + Utilities.base64Encode(name + ":" + id)
        }

        var option = {
            'method': 'get',
            'contentType': 'application/json',
            'headers': header,
        };

        var jenkinsDomain = userProperties.getProperty("jenkinsDomain");
        var jenkinsJobCategory = userProperties.getProperty("jenkinsJobCategory");

        if (!configurationValidate(jenkinsDomain, jenkinsJobCategory)) {
            SpreadsheetApp.getUi().alert('Please set Jenkins Domain and JobCategory in Addon Configuration');
        } else {
            return [jenkinsDomain, jenkinsJobCategory, option];
        }
    }
}