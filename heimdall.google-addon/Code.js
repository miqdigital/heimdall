var mainSheet = SpreadsheetApp.getActiveSheet();

// Gets the sheet range - starts from 1st row and column and contains one row in range
var headers = mainSheet.getRange(1, 1, 1, mainSheet.getLastColumn()).getValues();

/**
 *  Returns the user properties
 */
function getAuthValues() {
    return userProperties.getProperties();
}

/**
 *  Triggered when the spreadsheet is open
 */
function onOpen(e) {
    var ui = SpreadsheetApp.getUi();
    ui.createAddonMenu().addItem('Configurations', 'configureUserRelatedInfo').addSeparator()
        .addItem("Test Execution", "startTestRun").addSeparator().addItem("UpdateBuildInfo", "getAllBuildCollection").addToUi();
}

/**
 *  Event handler for Configurations sub-menu
 */
function configureUserRelatedInfo() {
    var htmlOutput = HtmlService.createHtmlOutputFromFile('config').setSandboxMode(HtmlService.SandboxMode.IFRAME).setHeight(400).setWidth(400);
    SpreadsheetApp.getUi().showModelessDialog(htmlOutput, "User properties");
}

/**
 *  Triggered when Test Execution sub-menu in the add-on menu is selected
 */
function startTestRun() {
    var htmlOutput = HtmlService.createHtmlOutputFromFile('execution').setSandboxMode(HtmlService.SandboxMode.IFRAME).setHeight(600).setWidth(400);
    SpreadsheetApp.getUi().showModelessDialog(htmlOutput, "Heimdal")
}

/**
 *  Invoked when Add-on is installed
 */
function onInstall(e) {
    onOpen(e)
}