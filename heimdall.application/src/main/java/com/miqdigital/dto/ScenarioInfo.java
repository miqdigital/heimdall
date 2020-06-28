package com.miqdigital.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ScenarioInfo {
  private String scenarioTagId;
  private String scenarioName;
  private String scenarioStatus;
  private String scenarioTotalDuration;
  private int scenarioTotalSteps;
  private String featureDescription;
  private String featureFile;
  private ScenarioStep scenarioStep;
}