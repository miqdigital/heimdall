var Issue_Key = "Issue Key"
var Issue_Key_COL;

var Suite = "Suite"
var Suite_COL;

var Test_Type = "Test type"
var Test_Type_COL;

var Status = "Status"
var Status_COL;

var failedStatus = "FAILED"

var min_build = 0;
var max_build = 0;

var RED_COLOR = "#FF0000"
var GREEN_COLOR = "#00FF00"
var LIGHTER_RED = "#FFCCCC"
var YELLOW_COLOR = "#FFFF00"
var WHITE_COLOR = "#FFFFFF"

var HTTP_RESPONSE_GREATER_THAN_299 = 299;

Issue_Key_COL = headers[0].indexOf(Issue_Key);
Suite_COL = headers[0].indexOf(Suite);
Status_COL = headers[0].indexOf(Status);
Test_Type_COL = headers[0].indexOf(Test_Type);

var userProperties = PropertiesService.getUserProperties();
var scriptProperties = PropertiesService.getScriptProperties();

var buildsAlreadyPresentInSheet = [];
var build_Nos = headers[0].filter(function (element) {
    return /BUILD-/.test(element);
})
build_Nos.filter(function (element) {
    var num = element.split("-")[1];
    buildsAlreadyPresentInSheet.push(num)
})

//getLatest build and loop all the build values in s3 till the latest one.
if (buildsAlreadyPresentInSheet.length > 0) {
    buildsAlreadyPresentInSheet.sort()
    min_build = buildsAlreadyPresentInSheet[0]
    max_build = buildsAlreadyPresentInSheet[buildsAlreadyPresentInSheet.length - 1]
    Logger.log(max_build)
    Logger.log(min_build)
} else {
    Logger.log("no builds in sheets")
}