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
   * The constant channelName.
   */
  private String channelName;
  /**
   * The constant isNotifySlack.
   */
  private boolean isNotifySlack;
  /**
   * The constant jiraPrefix.
   */
  private String jiraPrefix;

  /**
   * The constant s3BucketFolderName e.g. ABCCompanyAutomationTestReport
   */
  private String s3BucketName;

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
    isNotifySlack = Boolean.parseBoolean(properties.getProperty("ISNOTIFYSLACK"));
    jiraPrefix = properties.getProperty("JIRA_PREFIX");
    s3BucketName = properties.getProperty("S3_BUCKETNAME");
  }

}