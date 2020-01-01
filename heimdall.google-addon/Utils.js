/**
 *  Removes if there is any filter in the spreadSheet's Sheet
 *  @param {Sheet} sheet is an Sheet Object
 */

function removeIfAnyFilter(sheet) {
    while (sheet.getFilter() != null) {
        sheet.getFilter().remove();
    }
}

/**
 *  Creates Filters to the given sheet
 *  @param {Sheet} sheet is an Sheet Object
 *  @param {Array_Of_values} value to apply filter for a column
 *  @param {Integer} column to apply filter to that column
 */

function createFilterAndApplyToSheet(sheet, value, column) {
    var fcb = SpreadsheetApp.newFilterCriteria().whenTextDoesNotContain(value);
    var filter = sheet.getRange(1, column + 1, sheet.getLastRow()).createFilter()
        .setColumnFilterCriteria(column + 1, fcb.build());
}

/**
 *  It returns the indexes of the hidden Rows when we apply filter
 *  @param {String} ssId is an SpreadSheet Id
 *  @param {String} sheetId is an id of a sheet in spreadSheet
 */

function getHiddenRowsIndex(ssId, sheetId) {
    var hiddenRows = [];
    // to use this Sheets class , enable google sheets api in the https://console.developers.google.com/apis/dashboard
    var fields = "sheets(data(rowMetadata(hiddenByFilter)),properties/sheetId)";
    var sheets = Sheets.Spreadsheets.get(ssId, {
        fields: fields
    }).sheets;

    for (var i = 0; i < sheets.length; i++) {
        if (sheets[i].properties.sheetId == sheetId) {
            var data = sheets[i].data;
            var rows = data[0].rowMetadata;
            for (var j = 0; j < rows.length; j++) {
                if (rows[j].hiddenByFilter) hiddenRows.push(j);
            }
            break;
        }
    }
    return hiddenRows;
}

/**
 *  It returns the rows in a sheet by giving the array rowIndexes.
 *  @param {Sheet} sheet is an Sheet Object.
 *  @param {Array} indexOfRows is an array which contains indexes of a sheet.
 */
function getRowsInSpreadSheet(sheet, indexOfRows) {
    var filteredRows = [];
    indexOfRows.map(function (index) {
        var single_row = sheet.getRange(index + 1, 1, 1, sheet.getLastColumn()).getValues()
        single_row.map(function (ele) {
            filteredRows.push(ele);
        })
    })
    return filteredRows;
}

/**
 *   Returns the array of rows after filtering is applied in a sheet
 *   @param {String} value
 */

function getFilteredRowValues(value) {
    var ssId = SpreadsheetApp.getActive().getId();
    var sheetId = SpreadsheetApp.getActiveSheet().getSheetId();
    var ss = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
    removeIfAnyFilter(ss);
    //create filter and apply to a sheet
    createFilterAndApplyToSheet(ss, value, Test_Type_COL);
    var hiddenRows = getHiddenRowsIndex(ssId, sheetId);
    removeIfAnyFilter(ss);
    return getRowsInSpreadSheet(ss, hiddenRows)
}

/**
 *   Returns Array of unique Values in a suite column in json format
 */
function getAvailableSuites() {

    var uniqueSuitesValues = [];
    var ss = SpreadsheetApp.getActiveSheet();
    var suite_col_values = ss.getRange(1, Suite_COL + 1, ss.getLastRow()).getValues();

    for (var i = 1; i < suite_col_values.length; i++) {
        for (var j = 0; j < suite_col_values[i].length; j++) {
            if (uniqueSuitesValues.indexOf(suite_col_values[i][j]) == -1) {
                uniqueSuitesValues.push(suite_col_values[i][j])
            }
        }
    }
    return {
        "unique_suites_Values": uniqueSuitesValues
    }

}

/**
 *   creates the sheet in the spreadSheet
 *   @param {String} name of the sheet
 */
function createASheetInAGivenSpreadSheet(name) {
    SpreadsheetApp.getActiveSpreadsheet().insertSheet(name);
}
/**
 *  User configurations are set
 *  @param {String} user properties
 */
function getAuthenticationValues(auth) {
    userProperties.setProperties(auth)
    SpreadsheetApp.getUi().alert("Configuration Values are set")
}

/*
 * Returns the list of test Ids for selected suites
 * @params {list_of_tags} list of test case tags
 */
function getFilteredRowsOfSuites(list_of_tags) {

    var k = [];
    var ss = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet().getDataRange();
    var issue_values = ss.getDisplayValues();

    for (var i = 1; i < issue_values.length; i++) {
        if (list_of_tags.indexOf(issue_values[i][Suite_COL]) > -1) {
            k.push(issue_values[i][Issue_Key_COL]);
        }
    }

    var FilteredIssueIdList = "@" + k.join(",@");

    return {
        "filtered_issue_id_list": FilteredIssueIdList
    }

}

/**
 *   returns unique sheet name for every run
 */
function getTimeStamp() {
    var Dates = new Date();
    return Dates.toLocaleString();
}

/**
 *   Returns the array of column values in a particular sheet
 *   @param {Sheet} sheet is a Sheet Object in a spreadSheet
 *   @param {Integer} Col_index is a column index
 */
function getColumnValues(sheet, Col_index) {
    var col_values = []
    sheet.getRange(1, Col_index + 1, sheet.getLastRow()).getValues().map(function (col_value) {
        col_values.push(col_value[0])
    });
    return col_values;
}