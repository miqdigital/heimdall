package com.miqdigital.dto;

import lombok.Builder;

@Builder
public class BuildResultDto {
  public StringBuilder testExecutionInfo;
  public StringBuilder failedTestDescription;
  public long failedTestCount;
}