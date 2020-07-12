package com.miqdigital.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EmailDto {
  private final boolean notifyEmail;
  private final String emailTo;
  private final String emailFrom;
  private final String smtpHost;
  private final String smtpPort;
  private final String smtpUsername;
  private final String smtpPassword;
  private final String emailSubject;

}
