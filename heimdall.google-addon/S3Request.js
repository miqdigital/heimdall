function S3Request(service) {
    this.service = service;

    this.httpMethod = "GET";
    this.contentType = "";
    this.content = ""; //content of the HTTP request
    this.bucket = ""; //gets turned into host (bucketName.s3.amazonaws.com)
    this.objectName = "";
    this.headers = {};

    this.date = new Date();
}

S3Request.prototype.setContentType = function (contentType) {
    if (typeof contentType != 'string') throw 'contentType must be passed as a string';
    this.contentType = contentType;
    return this;
};

S3Request.prototype.getContentType = function () {
    var contentType = "";
    if (this.contentType) {
        contentType = this.contentType;
    } else {
        //if no contentType has been explicitly set, default based on HTTP methods
        if (this.httpMethod == "PUT" || this.httpMethod == "POST") {
            //UrlFetchApp defaults to this for these HTTP methods
            contentType = "application/x-www-form-urlencoded";
        }
    }
    return contentType;
}

S3Request.prototype.setContent = function (content) {
    if (typeof content != 'string') throw 'content must be passed as a string'
    this.content = content;
    return this;
};

S3Request.prototype.setHttpMethod = function (method) {
    if (typeof method != 'string') throw "http method must be string";
    this.httpMethod = method;
    return this;
};

S3Request.prototype.setBucket = function (bucket) {
    if (typeof bucket != 'string') throw "bucket name must be string";
    this.bucket = bucket;
    return this;
};

S3Request.prototype.setObjectName = function (objectName) {
    if (typeof objectName != 'string') throw "objectName must be string";
    this.objectName = objectName;
    return this;
};

S3Request.prototype.addHeader = function (name, value) {
    if (typeof name != 'string') throw "header name must be string";
    if (typeof value != 'string') throw "header value must be string";
    this.headers[name] = value;
    return this;
};

S3Request.prototype.getUrl = function () {
    return "http://" + this.bucket.toLowerCase() + ".s3.amazonaws.com/" + this.objectName;
};

S3Request.prototype.execute = function (options) {
    options = options || {};

    this.headers.Authorization = this.getAuthHeader_();
    this.headers.Date = this.date.toUTCString();
    if (this.content.length > 0) {
        this.headers["Content-MD5"] = this.getContentMd5_();
    }

    var params = {
        method: this.httpMethod,
        payload: this.content,
        headers: this.headers,
        muteHttpExceptions: true //get error content in the response
    }

    //only add a ContentType header if non-empty (although should be OK either way)

    if (this.getContentType()) {
        params.contentType = this.getContentType();
    }

    Logger.log("header printing")
    Logger.log(this.headers)
    var response = UrlFetchApp.fetch(this.getUrl(), params);
    Logger.log(response)

    //Used for debugging purpose
    var request = UrlFetchApp.getRequest(this.getUrl(), params);

    //Log request and response
    this.lastExchangeLog = this.service.logExchange_(request, response);
    if (options.logRequests) {
        Logger.log(this.service.getLastExchangeLog());
    }

    //used in case you want to peak at the actual raw HTTP request coming out of Google's UrlFetchApp infrastructure
    if (options.echoRequestToUrl) {
        UrlFetchApp.fetch(options.echoRequestToUrl, params);
    }

    //check for error codes (AWS uses variants of 200s for flavors of success)
    if (response.getResponseCode() > HTTP_RESPONSE_GREATER_THAN_299) {
        //convert XML error response from AWS into JS object, and give it a name
        var error = {};
        error.name = "AwsError";
        try {
            var errorXmlElements = XmlService.parse(response.getContentText()).getRootElement().getChildren();

            for (i in errorXmlElements) {
                var name = errorXmlElements[i].getName();
                name = name.charAt(0).toLowerCase() + name.slice(1);
                error[name] = errorXmlElements[i].getText();
            }
            error.toString = function () {
                return "AWS Error - " + this.code + ": " + this.message;
            };
            error.httpRequestLog = this.service.getLastExchangeLog();
        } catch (e) {
            //error parsing XML error response from AWS (will obscure actual error)
            error.message = "AWS returned HTTP code " + response.getResponseCode() + ", but error content could not be parsed."
            error.toString = function () { return this.message; };
            error.httpRequestLog = this.service.getLastExchangeLog();
        }
        Logger.log("Error during execution : " + error);
        throw error;
    }
    return response;
};


S3Request.prototype.getAuthHeader_ = function () {

    var stringToSign = this.httpMethod + "\n";

    var contentLength = this.content.length;
    stringToSign += this.getContentMd5_() + "\n";
    stringToSign += this.getContentType() + "\n";

    //set expires time 60 seconds into future
    stringToSign += this.date.toUTCString() + "\n";
    var amzHeaders = [];

    for (var headerName in this.headers) {
        if (headerName.match(/^x-amz/i)) {
            var header = headerName.toLowerCase() + ":" + this.headers[headerName].replace(/\s+/, " ");
            amzHeaders.push(header)
        }
    }

    if (amzHeaders.length > 0) {
        stringToSign += amzHeaders.sort().join("\n") + "\n";
    }

    var canonicalizedResource = "/" + this.bucket.toLowerCase() + this.getUrl().replace("http://" + this.bucket.toLowerCase() + ".s3.amazonaws.com", "");
    stringToSign += canonicalizedResource;

    Logger.log("Url replace string to sign - " + stringToSign)
    var signature = Utilities.base64Encode(Utilities.computeHmacSignature(Utilities.MacAlgorithm.HMAC_SHA_1,
        stringToSign,
        this.service.secretAccessKey,
        Utilities.Charset.UTF_8));

    return "AWS " + this.service.accessKeyId + ':' + signature;
};


S3Request.prototype.getContentMd5_ = function () {
    if (this.content.length > 0) {
        return Utilities.base64Encode(Utilities.computeDigest(Utilities.DigestAlgorithm.MD5, this.content, Utilities.Charset.UTF_8));
    } else {
        return "";
    }
};