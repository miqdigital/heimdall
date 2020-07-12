package com.miqdigital.dto;

import lombok.Builder;

@Builder
public class ScenarioStepDto {
  private final String errMessage;
  private final String stepDuration;
  private final String scenarioLine;
}