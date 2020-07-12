package com.miqdigital.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ScenarioInfoDto {
  private final String scenarioTagId;
  private final String scenarioName;
  private final String scenarioStatus;
  private final String scenarioTotalDuration;
  private final int scenarioTotalSteps;
  private final String featureDescription;
  private final String featureFile;
  private final ScenarioStepDto scenarioStepDto;
}