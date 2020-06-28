package com.miqdigital.services;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.miqdigital.dto.NotificationDto;
import com.miqdigital.dto.ExecutionInfo;
import com.miqdigital.dto.ScenarioInfo;
import com.miqdigital.dto.BuildResultDto;

/**
 * This class builds the Slack message and the failed test scenarios report, if any.
 */

public class MessageGenerator {

  private MessageGenerator() {
  }

  public static BuildResultDto getBuildResult(final ExecutionInfo executionInfo,
      NotificationDto notificationDto) {
    return getBuildResponse(executionInfo, notificationDto);
  }

  /**
   * Sends the build notification to the Slack channel.
   *
   * @param executionInfo test execution info
   * @param notificationDto
   * @return slack notification info
   */
  private static BuildResultDto getBuildResponse(final ExecutionInfo executionInfo,
      NotificationDto notificationDto) {
    final StringBuilder testExecutionInfo = new StringBuilder();
    StringBuilder failedTestsInfo = new StringBuilder();

    final StringBuilder jenkinsBuildInfo = getBuildInfo(executionInfo);

    if (executionInfo.failTestCount > 0) {
      testExecutionInfo.append("*BUILD FAILED ").append(jenkinsBuildInfo)
          .append("\n*Tests Failed:* ").append(executionInfo.failTestCount)
          .append("\n*List of failed tests:* ");

      failedTestsInfo = getFailedTestInfo(executionInfo);
    } else {
      testExecutionInfo.append("*BUILD PASSED ").append(jenkinsBuildInfo);
    }

    if (Objects.nonNull(executionInfo.BuildNumber) && Objects
        .nonNull(System.getProperty("viewName"))) {
      String jenkinsJobUrl = notificationDto.getJenkinsDomain() + "/view/%s/job/%s/%s/console";
      testExecutionInfo.append("\n").append("*Console out:* ").append(String
          .format(jenkinsJobUrl, System.getProperty("viewName"), executionInfo.BuildName,
              executionInfo.BuildNumber));
    }

    return BuildResultDto.builder().testExecutionInfo(testExecutionInfo)
        .failedTestDescription(failedTestsInfo).failedTestCount(executionInfo.failTestCount)
        .build();
  }

  /**
   * @param executionInfo test execution info
   * @return get failed test information
   */
  private static StringBuilder getFailedTestInfo(final ExecutionInfo executionInfo) {
    final StringBuilder failedTestsInfo = new StringBuilder();
    final List<ScenarioInfo> scenarioInfoList = executionInfo.scenarioInfoList;
    final List<ScenarioInfo> failedTests =
        scenarioInfoList.stream().filter(r -> r.getScenarioStatus().equals("FAILED"))
            .collect(Collectors.toList());
    for (final ScenarioInfo scenarioInfo : failedTests) {
      failedTestsInfo.append(scenarioInfo.getScenarioTagId()).append(" ");
      failedTestsInfo.append(scenarioInfo.getScenarioName()).append("\n");
    }
    failedTestsInfo.setLength(failedTestsInfo.length() - 1);
    return failedTestsInfo;
  }

  /**
   * @param executionInfo test execution info
   * @return jenkins build info
   */
  private static StringBuilder getBuildInfo(final ExecutionInfo executionInfo) {
    return new StringBuilder().append("for job:* ").append(executionInfo.BuildName)
        .append("\n*BuildNo.:* ").append(executionInfo.BuildNumber).append("\n*Environment:* ")
        .append(executionInfo.environment).append("\n*Test type:* ").append(executionInfo.testType)
        .append("\n*Total tests executed:* ").append(executionInfo.totalTests)
        .append("\n*Tests Passed:* ").append(executionInfo.passTestCount);
  }
}