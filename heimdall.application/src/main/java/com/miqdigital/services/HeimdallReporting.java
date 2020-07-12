package com.miqdigital.services;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.miqdigital.dto.BuildResultDto;
import com.miqdigital.dto.EmailDto;
import com.miqdigital.dto.ExecutionInfoDto;
import com.miqdigital.dto.SlackDto;
import com.miqdigital.utils.AmazonS3Connector;
import com.miqdigital.utils.EmailUtil;
import com.miqdigital.utils.ReadProperties;
import com.miqdigital.utils.SlackUtils;

import io.cucumber.core.exception.CucumberException;

/**
 * This class uploads the build status to S3 and notifies the Slack channel.
 */
public class HeimdallReporting {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeimdallReporting.class);

  private final ReadProperties properties;
  private final String executionOutputPath;

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
   *
   * @param buildResultDto Jenkins build execution info
   * @param slackDto slack channel info from client
   */
  private static void sendSlackNotification(final BuildResultDto buildResultDto,
      final SlackDto slackDto) throws IOException {
    SlackUtils slackUtils = new SlackUtils();

    slackUtils.sendSlackNotification(slackDto, buildResultDto);
    LOGGER.info("Slack Notification Sent");
  }

  /**
   * Sends Email notification.
   *
   * @param buildResultDto
   * @param emailDto
   */
  private static void sendEmailNotification(final BuildResultDto buildResultDto, EmailDto emailDto)
      throws MessagingException, IOException {

    EmailUtil.sendEmail(emailDto, buildResultDto);
    LOGGER.info("Email Sent");
  }

  /**
   * Pushes to S3 only if jenkins buildNumber is present.
   *
   * @param bucketName pass the S3 bucketName
   * @param executionInfoDto test execution info
   */
  private void pushResultToS3(final String bucketName, final ExecutionInfoDto executionInfoDto)
      throws IOException, InterruptedException {

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    if (Objects.nonNull(executionInfoDto.BuildNumber)) {
      final String outputReportPath =
          System.getProperty("user.dir") + "/target/" + executionInfoDto.BuildNumber + ".json";

      try (final FileWriter writer = new FileWriter(outputReportPath)) {
        objectMapper.writeValue(writer, executionInfoDto);
      }
      final AmazonS3Connector amazonS3Connector = new AmazonS3Connector.Builder().build();
      final String prefix =
          executionInfoDto.BuildName + "/" + executionInfoDto.BuildNumber + ".json";
      amazonS3Connector.uploadFile(outputReportPath, bucketName, prefix);
    } else {
      LOGGER.info("Build number is null, not pushing build result to S3");
    }
  }

  /**
   * Updates the execution result status in S3.
   *
   * @throws IllegalAccessException illegalAccessException
   * @throws NoSuchFieldException noSuchFieldException
   * @throws IOException inputOutputException
   * @throws InterruptedException interruptedException
   */
  public void updateStatusInS3()
      throws IllegalAccessException, NoSuchFieldException, IOException, InterruptedException {
    ExecutionInfoGenerator executionInfoGenerator = new ExecutionInfoGenerator();
    ExecutionInfoDto executionInfoDto =
        executionInfoGenerator.getBuildExecutionDetails(properties, executionOutputPath);

    pushResultToS3(properties.getS3BucketName(), executionInfoDto);
  }

  /**
   * Generates the execution info and pushes the results to S3 and notifies on the Slack channel.
   *
   * @throws NoSuchFieldException noSuchFieldException
   * @throws IllegalAccessException illegalAccessException
   */
  public void updateStatusInS3AndNotify()
      throws NoSuchFieldException, IllegalAccessException, IOException, InterruptedException,
      MessagingException {
    ExecutionInfoGenerator executionInfoGenerator = new ExecutionInfoGenerator();
    ExecutionInfoDto executionInfoDto =
        executionInfoGenerator.getBuildExecutionDetails(properties, executionOutputPath);

    if (StringUtils.isNullOrEmpty(properties.getChannelName()) || StringUtils
        .isNullOrEmpty(properties.getHeimdallBotToken())) {
      throw new NullPointerException(
          "Please specify Channel name and Slack bot token in runner properties file");
    }

    SlackDto slackDto = SlackDto.builder().slackChannel(properties.getChannelName())
        .heimdallBotToken(properties.getHeimdallBotToken()).notifySlack(properties.isNotifySlack())
        .build();

    final EmailDto emailDto = EmailDto.builder().smtpHost(properties.getSmtpHost())
        .smtpUsername(properties.getSmtpUsername()).smtpPassword(properties.getSmtpPassword())
        .notifyEmail(properties.isNotifyEmail()).emailSubject(properties.getEmailSubject())
        .emailFrom(properties.getEmailFrom()).emailTo(properties.getEmailTo())
        .smtpPort(properties.getSmtpPort()).build();

    LOGGER.info("Execution info generated Successfully");

    final BuildResultDto buildResultDto =
        MessageGenerator.getBuildResult(executionInfoDto, properties.getJenkinsDomain());

    if (slackDto.isNotifySlack()) {
      sendSlackNotification(buildResultDto, slackDto);
    }
    if (emailDto.isNotifyEmail()) {
      sendEmailNotification(buildResultDto, emailDto);
    }
    pushResultToS3(properties.getS3BucketName(), executionInfoDto);
  }

}