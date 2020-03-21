package com.miqdigital.execution.dto;

import java.util.Arrays;
import java.util.List;

import com.miqdigital.scenario.dto.ScenarioInfo;

import lombok.Builder;

@Builder
public class ExecutionInfo {
  public String environment;
  public String testType;
  public String dateTime;
  public long totalTests;
  public long passTestCount;
  public long failTestCount;
  public String BuildName;
  public String BuildNumber;
  public List<ScenarioInfo> scenarioInfoList;

  @Override
  public String toString() {
    return "ExecutionInfo{" + "environment='" + environment + '\'' + ", testType='" + testType
        + '\'' + ", dateTime='" + dateTime + '\'' + ", totalTests=" + totalTests
        + ", passTestCount=" + passTestCount + ", failTestCount=" + failTestCount + ", BuildName='"
        + BuildName + '\'' + ", BuildNumber='" + BuildNumber + '\'' + ", scenarioInfoList=" + Arrays
        .toString(scenarioInfoList.toArray()) + '}';
  }
}
