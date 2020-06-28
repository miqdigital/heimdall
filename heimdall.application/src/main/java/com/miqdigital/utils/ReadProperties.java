package com.miqdigital.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;

/**
 * The type Read properties file.
 */
@Getter
public class ReadProperties {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ReadProperties.class);

  /**
   * The constant heimdallBotToken.
   */
  private String heimdallBotToken;
  /**
   * The constant channelName.
   */
  private String channelName;
  /**
   * The constant notifySlack.
   */
  private boolean notifySlack;
  /**
   * The constant notifyEmail.
   */
  private boolean notifyEmail;
  /**
   * The constant emailTo.
   */
  private String emailTo;
  /**
   * The constant emailFrom.
   */
  private String emailFrom;
  /**
   * The constant emailPort.
   */
  private String emailPort;
  /**
   * The constant emailSubject.
   */
  private String emailSubject;
  /**
   * The constant jiraPrefix.
   */
  private String jiraPrefix;

  /**
   * The constant s3BucketFolderName e.g. ABCCompanyAutomationTestReport
   */
  private String s3BucketName;
  /**
   * The host to use while sending email.
   */
  private String emailHost;
  /**
   * The SMTP username.
   */
  private String smtpUsername;
  /**
   * The SMTP password.
   */
  private String smtpPassword;
  /**
   * The company's Jenkins domain
   */
  private String jenkinsDomain;

  private Properties properties;

  /**
   * Instantiates a new Read properties file.
   */
  public ReadProperties(final String filepath) throws IOException {
    final File file = new File(filepath);
    final FileInputStream fileInputStream = new FileInputStream(file);
    this.properties = new Properties();
    properties.load(fileInputStream);
    initFields();
  }

  /**
   * To initialize constants.
   */
  private void initFields() {
    channelName = properties.getProperty("CHANNEL_NAME");
    notifySlack = Boolean.parseBoolean(properties.getProperty("NOTIFY_SLACK"));
    heimdallBotToken = properties.getProperty("HEIMDALL_BOT_TOKEN");
    jiraPrefix = properties.getProperty("JIRA_PREFIX");
    s3BucketName = properties.getProperty("S3_BUCKETNAME");
    emailHost = properties.getProperty("EMAIL_HOST");
    smtpUsername = properties.getProperty("SMTP_USERNAME");
    smtpPassword = properties.getProperty("SMTP_PASSWORD");
    notifyEmail = Boolean.parseBoolean(properties.getProperty("NOTIFY_EMAIL"));
    emailTo = properties.getProperty("EMAIL_TO");
    emailFrom = properties.getProperty("EMAIL_FROM");
    emailPort = properties.getProperty("EMAIL_PORT");
    emailSubject = properties.getProperty("EMAIL_SUBJECT");
    jenkinsDomain = properties.getProperty("JENKINS_DOMAIN");
  }

}