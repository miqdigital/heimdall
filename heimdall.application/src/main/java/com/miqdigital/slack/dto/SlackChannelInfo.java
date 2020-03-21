package com.miqdigital.slack.dto;

import lombok.Builder;

@Builder
public class SlackChannelInfo {
  public String channelName;
  public String token;
  public boolean isNotifySlack;
}