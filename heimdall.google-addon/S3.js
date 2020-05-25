/* constructs an S3 service
 *
 * @constructor
 * @param {string} accessKeyId your AWS AccessKeyId
 * @param {string} secretAccessKey your AWS SecretAccessKey
 * @param {Object} options key-value object of options, unused
 *
 * @return {S3}
 */
function getInstance(accessKeyId, secretAccessKey, options) {
    return new S3(accessKeyId, secretAccessKey, options);
}

/* constructs an S3 service
 *
 * @constructor
 * @param {string} accessKeyId your AWS AccessKeyId
 * @param {string} secretAccessKey your AWS SecretAccessKey
 * @param {Object} options key-value object of options, unused
 */
function S3(accessKeyId, secretAccessKey, options) {
    if (typeof accessKeyId !== 'string') throw "Must pass accessKeyId to S3 constructor";
    if (typeof secretAccessKey !== 'string') throw "Must pass secretAcessKey to S3 constructor";

    this.accessKeyId = accessKeyId;
    this.secretAccessKey = secretAccessKey;
    this.options = options || {};
}


/* gets object from S3 bucket
 *
 * @param {string} bucket name of bucket
 * @param {string} objectName name that uniquely identifies object within bucket
 * @param {Object} options optional parameters for get request (unused)
 * @throws {Object} AwsError on failure
 * @return {Blob|Object} data value, converted from JSON or as a Blob if it was something else; null if it doesn't exist
 */
S3.prototype.getObject = function (bucket, objectName, options) {
    options = options || {};

    var request = new S3Request(this);
    request.setHttpMethod('GET');
    request.setContentType('application/json')
    request.setBucket(bucket);
    if (typeof bucket !== 'string'){
         SpreadsheetApp.getUi().alert('Pass valid bucket name');
    }
    request.setObjectName(objectName);
    try {
        var responseBlob = request.execute(options).getBlob();
    } catch (e) {
        if (e.name == "AwsError" && e.code == 'NoSuchKey') {
            Logger.log("No such object present in S3");
            return null;
        } else {
            Logger.log("Error in getting S3 object : " + e);
            throw e;
        }
    }

    if (responseBlob.getContentType() == "application/json") {
        return JSON.parse(responseBlob.getDataAsString());
    }
    return responseBlob.getDataAsString();
};

//Used for debugging
S3.prototype.getLastExchangeLog = function () {
    return this.lastExchangeLog;
}

/*
 * helper to format log entry about HTTP request/response
 *
 * @param {Object} request object, from UrlFetchApp.getRequest()
 * @param {goog.HTTPResponse} response object, from UrlFetchApp
 */
S3.prototype.logExchange_ = function (request, response) {
    var logContent = "";
    logContent += "\n-- REQUEST --\n";
    for (i in request) {
        if (typeof request[i] == 'string' && request[i].length > 1000) {
            //truncate to avoid making log unreadable
            request[i] = request[i].slice(0, 1000) + " ... [TRUNCATED]";
        }
        logContent += Utilities.formatString("\t%s: %s\n", i, request[i]);
    }

    logContent += "-- RESPONSE --\n";
    logContent += "HTTP Status Code: " + response.getResponseCode() + "\n";
    logContent += "Headers:\n";

    var headers = response.getHeaders();
    for (i in headers) {
        logContent += Utilities.formatString("\t%s: %s\n", i, headers[i]);
    }
    logContent += "Body:\n" + response.getContentText();
    this.lastExchangeLog = logContent;
}