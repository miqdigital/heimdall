package com.miqdigital.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SlackDto {
  private final String slackChannel;
  private final String heimdallBotToken;
  private final boolean notifySlack;
}
