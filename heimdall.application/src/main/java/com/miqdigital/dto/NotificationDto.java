package com.miqdigital.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EmailDto {
  private final String emailHost;
  private final String smtpUsername;
  private final String smtpPassword;
  private final boolean notifySlack;
  private final boolean notifyEmail;
  private final String emailTo;
  private final String emailFrom;
  private final String subject;
  private final String jenkinsDomain;

}
