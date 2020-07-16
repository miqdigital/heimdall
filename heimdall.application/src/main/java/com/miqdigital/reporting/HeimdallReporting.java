package com.miqdigital.reporting;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.util.StringUtils;
import com.google.gson.Gson;
import com.miqdigital.connector.AmazonS3Connector;
import com.miqdigital.execution.ExecutionInfoGenerator;
import com.miqdigital.execution.dto.ExecutionInfo;
import com.miqdigital.slack.MessageGenerator;
import com.miqdigital.slack.SlackUtils;
import com.miqdigital.slack.dto.SlackChannelInfo;
import com.miqdigital.slack.dto.SlackMessageInfo;
import com.miqdigital.utility.ReadProperties;

import cucumber.runtime.CucumberException;

/**
 * This class uploads the build status to S3 and notifies the Slack channel.
 */
public class HeimdallReporting {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeimdallReporting.class);
  private ReadProperties readProperties;
  private String executionOutputPath;

  public HeimdallReporting(final String pathOfRunnerPropertiesFile,
      final String executionOutputPath) throws IOException {
    if (StringUtils.isNullOrEmpty(pathOfRunnerPropertiesFile)) {
      throw new FileNotFoundException("Please specify path of the runner properties file");
    } else if (StringUtils.isNullOrEmpty(executionOutputPath)) {
      throw new CucumberException("Please specify cucumber output path");
    } else {
      this.readProperties = new ReadProperties(pathOfRunnerPropertiesFile);
      this.executionOutputPath = executionOutputPath;
    }
  }

  /**
   * Sends the notification on the Slack channel.
   *
   * @param executionInfo    Jenkins build execution info
   * @param slackChannelInfo slack channel info from client
   */
  private static void sendSlackNotification(final ExecutionInfo executionInfo,
      final SlackChannelInfo slackChannelInfo) throws IOException {
    final SlackMessageInfo slackMessageInfo = MessageGenerator.getMessage(executionInfo);
    SlackUtils slackUtils = new SlackUtils();

    slackUtils.slackNotification(slackChannelInfo, slackMessageInfo);
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
        executionInfoGenerator.getBuildExecutionDetails(readProperties, executionOutputPath);

    pushResultToS3(readProperties.getS3BucketName(), executionInfo);
  }

  /**
   * Generates the execution info and pushes the results to S3 and notifies on the Slack channel.
   *
   * @throws NoSuchFieldException   noSuchFieldException
   * @throws IllegalAccessException illegalAccessException
   */
  public void updateStatusInS3AndNotifySlack()
      throws NoSuchFieldException, IllegalAccessException, IOException, InterruptedException {
    ExecutionInfoGenerator executionInfoGenerator = new ExecutionInfoGenerator();
    ExecutionInfo executionInfo =
        executionInfoGenerator.getBuildExecutionDetails(readProperties, executionOutputPath);

    String heimdallSlackToken = System.getProperty("HeimdallSlackToken");

    if (StringUtils.isNullOrEmpty(readProperties.getChannelName())) {
      throw new NullPointerException("Please specify Slack Channel name in runner properties file");

    } else if (StringUtils.isNullOrEmpty(heimdallSlackToken)) {
      heimdallSlackToken = readProperties.getHeimdallBotToken();
      if (StringUtils.isNullOrEmpty(heimdallSlackToken)) {
        throw new NullPointerException(
            "Please assign Slack bot token as system property in Jenkins or in runner properties "
                + "file");
      }
    }
    final SlackChannelInfo slackChannelInfo =
        SlackChannelInfo.builder().channelName(readProperties.getChannelName())
            .token(heimdallSlackToken).isNotifySlack(readProperties.isNotifySlack()).build();

    LOGGER.info("Execution info generated Successfully");
    if (slackChannelInfo.isNotifySlack) {
      sendSlackNotification(executionInfo, slackChannelInfo);
    }
    pushResultToS3(readProperties.getS3BucketName(), executionInfo);
  }

}