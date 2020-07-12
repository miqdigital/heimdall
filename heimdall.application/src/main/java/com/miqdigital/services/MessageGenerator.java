package com.miqdigital.services;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.miqdigital.dto.BuildResultDto;
import com.miqdigital.dto.ExecutionInfoDto;
import com.miqdigital.dto.ScenarioInfoDto;

/**
 * This class builds the Slack message and the failed test scenarios report, if any.
 */

public class MessageGenerator {

  private MessageGenerator() {
  }

  public static BuildResultDto getBuildResult(final ExecutionInfoDto executionInfoDto,
      String jenkinsDomain) {
    return getBuildResponse(executionInfoDto, jenkinsDomain);
  }

  /**
   * Sends the build notification to the Slack channel.
   *
   * @param executionInfoDto test execution info
   * @param jenkinsDomain
   * @return slack notification info
   */
  private static BuildResultDto getBuildResponse(final ExecutionInfoDto executionInfoDto,
      String jenkinsDomain) {
    final StringBuilder testExecutionInfo = new StringBuilder();
    StringBuilder failedTestsInfo = new StringBuilder();

    final StringBuilder jenkinsBuildInfo = getBuildInfo(executionInfoDto);

    if (executionInfoDto.failTestCount > 0) {
      testExecutionInfo.append("*BUILD FAILED ").append(jenkinsBuildInfo)
          .append("\n*Tests Failed:* ").append(executionInfoDto.failTestCount)
          .append("\n*List of failed tests:* ");

      failedTestsInfo = getFailedTestInfo(executionInfoDto);
    } else {
      testExecutionInfo.append("*BUILD PASSED ").append(jenkinsBuildInfo);
    }

    if (Objects.nonNull(executionInfoDto.BuildNumber) && Objects
        .nonNull(System.getProperty("viewName"))) {
      String jenkinsJobUrl = jenkinsDomain + "/view/%s/job/%s/%s/console";
      testExecutionInfo.append("\n").append("*Console out:* ").append(String
          .format(jenkinsJobUrl, System.getProperty("viewName"), executionInfoDto.BuildName,
              executionInfoDto.BuildNumber));
    }

    return BuildResultDto.builder().testExecutionInfo(testExecutionInfo)
        .failedTestDescription(failedTestsInfo).failedTestCount(executionInfoDto.failTestCount)
        .build();
  }

  /**
   * @param executionInfoDto test execution info
   * @return get failed test information
   */
  private static StringBuilder getFailedTestInfo(final ExecutionInfoDto executionInfoDto) {
    final StringBuilder failedTestsInfo = new StringBuilder();
    final List<ScenarioInfoDto> scenarioInfoDtoList = executionInfoDto.scenarioInfoDtoList;
    final List<ScenarioInfoDto> failedTests =
        scenarioInfoDtoList.stream().filter(r -> r.getScenarioStatus().equals("FAILED"))
            .collect(Collectors.toList());
    for (final ScenarioInfoDto scenarioInfoDto : failedTests) {
      failedTestsInfo.append(scenarioInfoDto.getScenarioTagId()).append(" ");
      failedTestsInfo.append(scenarioInfoDto.getScenarioName()).append("\n");
    }
    failedTestsInfo.setLength(failedTestsInfo.length() - 1);
    return failedTestsInfo;
  }

  /**
   * @param executionInfoDto test execution info
   * @return jenkins build info
   */
  private static StringBuilder getBuildInfo(final ExecutionInfoDto executionInfoDto) {
    return new StringBuilder().append("for job:* ").append(executionInfoDto.BuildName)
        .append("\n*BuildNo.:* ").append(executionInfoDto.BuildNumber).append("\n*Environment:* ")
        .append(executionInfoDto.environment).append("\n*Test type:* ")
        .append(executionInfoDto.testType).append("\n*Total tests executed:* ")
        .append(executionInfoDto.totalTests).append("\n*Tests Passed:* ")
        .append(executionInfoDto.passTestCount);
  }
}