package com.miqdigital.dto;

import java.util.Arrays;
import java.util.List;

import lombok.Builder;

@Builder
public class ExecutionInfoDto {
  public String environment;
  public String testType;
  public String dateTime;
  public long totalTests;
  public long passTestCount;
  public long failTestCount;
  public String BuildName;
  public String BuildNumber;
  public List<ScenarioInfoDto> scenarioInfoDtoList;

  @Override
  public String toString() {
    return "ExecutionInfoDto{" + "environment='" + environment + '\'' + ", testType='" + testType
        + '\'' + ", dateTime='" + dateTime + '\'' + ", totalTests=" + totalTests
        + ", passTestCount=" + passTestCount + ", failTestCount=" + failTestCount + ", BuildName='"
        + BuildName + '\'' + ", BuildNumber='" + BuildNumber + '\'' + ", scenarioInfoDtoList="
        + Arrays.toString(scenarioInfoDtoList.toArray()) + '}';
  }
}
