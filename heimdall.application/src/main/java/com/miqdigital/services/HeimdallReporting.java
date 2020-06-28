package com.miqdigital.reporting;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.util.StringUtils;
import com.google.gson.Gson;
import com.miqdigital.utils.AmazonS3Connector;
import com.miqdigital.dto.NotificationDto;
import com.miqdigital.execution.ExecutionInfoGenerator;
import com.miqdigital.execution.dto.ExecutionInfo;
import com.miqdigital.slack.MessageGenerator;
import com.miqdigital.slack.SlackUtils;
import com.miqdigital.dto.ResultDto;
import com.miqdigital.utils.EmailUtil;
import com.miqdigital.utils.ReadProperties;

import cucumber.runtime.CucumberException;

/**
 * This class uploads the build status to S3 and notifies the Slack channel.
 */
public class HeimdallReporting {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeimdallReporting.class);
  private ReadProperties properties;
  private String executionOutputPath;

  public HeimdallReporting(final String pathOfRunnerPropertiesFile,
      final String executionOutputPath) throws IOException {
    if (StringUtils.isNullOrEmpty(pathOfRunnerPropertiesFile)) {
      throw new FileNotFoundException("Please specify path of the runner properties file");
    } else if (StringUtils.isNullOrEmpty(executionOutputPath)) {
      throw new CucumberException("Please specify cucumber output path");
    } else {
      this.properties = new ReadProperties(pathOfRunnerPropertiesFile);
      this.executionOutputPath = executionOutputPath;
    }
  }

  /**
   * Sends the notification on the Slack channel.
   *  @param executionInfo    Jenkins build execution info
   * @param notificationDto slack channel info from client
   */
  private static void sendSlackNotification(final ExecutionInfo executionInfo,
      final NotificationDto notificationDto) throws IOException {
    final ResultDto resultDto = MessageGenerator.getBuildResult(executionInfo,notificationDto);
    SlackUtils slackUtils = new SlackUtils();

    slackUtils.sendSlackNotification(notificationDto, resultDto);
  }

  /**
   * Sends Email notification.
   * @param executionInfo
   * @param notificationDto
   */
  private static void sendEmailNotification(ExecutionInfo executionInfo,
      NotificationDto notificationDto) throws MessagingException, IOException {
    final ResultDto resultDto = MessageGenerator.getBuildResult(executionInfo, notificationDto);

    EmailUtil.sendEmail(notificationDto, resultDto);
  }

  /**
   * Pushes to S3 only if jenkins buildNumber is present.
   *
   * @param bucketName    pass the S3 bucketName
   * @param executionInfo test execution info
   */
  private void pushResultToS3(final String bucketName, final ExecutionInfo executionInfo)
      throws IOException, InterruptedException {

    LOGGER.info("Execution Result {}", executionInfo);
    if (Objects.nonNull(executionInfo.BuildNumber)) {
      final Gson gson = new Gson();
      final String outputReportPath =
          System.getProperty("user.dir") + "/target/" + executionInfo.BuildNumber + ".json";

      try (final FileWriter writer = new FileWriter(outputReportPath)) {
        gson.toJson(executionInfo, writer);
      }
      final AmazonS3Connector amazonS3Connector = new AmazonS3Connector.Builder().build();
      final String prefix = executionInfo.BuildName + "/" + executionInfo.BuildNumber + ".json";
      amazonS3Connector.uploadFile(outputReportPath, bucketName, prefix);
    } else {
      LOGGER.error("Build number is null, not pushing build result to S3");
    }
  }

  /**
   * Updates the execution result status in S3.
   *
   * @throws IllegalAccessException illegalAccessException
   * @throws NoSuchFieldException   noSuchFieldException
   * @throws IOException            inputOutputException
   * @throws InterruptedException   interruptedException
   */
  public void updateStatusInS3()
      throws IllegalAccessException, NoSuchFieldException, IOException, InterruptedException {
    ExecutionInfoGenerator executionInfoGenerator = new ExecutionInfoGenerator();
    ExecutionInfo executionInfo =
        executionInfoGenerator.getBuildExecutionDetails(properties, executionOutputPath);

    pushResultToS3(properties.getS3BucketName(), executionInfo);
  }

  /**
   * Generates the execution info and pushes the results to S3 and notifies on the Slack channel.
   *
   * @throws NoSuchFieldException   noSuchFieldException
   * @throws IllegalAccessException illegalAccessException
   */
  public void updateStatusInS3AndNotify()
      throws NoSuchFieldException, IllegalAccessException, IOException, InterruptedException,
      MessagingException {
    ExecutionInfoGenerator executionInfoGenerator = new ExecutionInfoGenerator();
    ExecutionInfo executionInfo =
        executionInfoGenerator.getBuildExecutionDetails(properties, executionOutputPath);

    if (StringUtils.isNullOrEmpty(properties.getChannelName()) || StringUtils
        .isNullOrEmpty(properties.getHeimdallBotToken())) {
      throw new NullPointerException(
          "Please specify Channel name and Slack bot token in runner properties file");
    }
    final NotificationDto notificationDto = NotificationDto.builder().emailHost(properties.getEmailHost())
        .slackChannel(properties.getChannelName())
        .slackToken(properties.getHeimdallBotToken())
        .notifySlack(properties.isNotifySlack())
        .smtpUsername(properties.getSmtpUsername())
        .smtpPassword(properties.getSmtpPassword())
        .notifyEmail(properties.isNotifyEmail())
        .emailFrom(properties.getEmailFrom())
        .emailTo(properties.getEmailTo())
        .jenkinsDomain(properties.getJenkinsDomain()).build();

    LOGGER.info("Execution info generated Successfully");
    if (notificationDto.isNotifySlack()) {
      sendSlackNotification(executionInfo, notificationDto);
    }
    if(notificationDto.isNotifyEmail()){
      sendEmailNotification(executionInfo, notificationDto);
    }
    pushResultToS3(properties.getS3BucketName(), executionInfo);
  }

}