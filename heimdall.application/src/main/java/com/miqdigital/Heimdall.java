package com.miqdigital;

import java.io.IOException;

import javax.mail.MessagingException;

import com.miqdigital.services.HeimdallReporting;

/**
 * This class uses the instance of Heimdall Reporting.
 */

public class Heimdall {

  private HeimdallReporting heimdallReporting;

  /**
   * Generated the report from cucumber output path and push status to S3 and notify on slack
   *
   * @param pathOfRunnerPropertiesFile Pass the pathOfRunnerPropertiesFile
   * @throws NoSuchFieldException   noSuchFieldException
   * @throws IllegalAccessException illegalAccessException
   */
  public void updateStatusInS3AndNotify(final String pathOfRunnerPropertiesFile,
      final String executionOutputPath)
      throws InterruptedException, IOException, IllegalAccessException, NoSuchFieldException,
      MessagingException {
    this.heimdallReporting = new HeimdallReporting(pathOfRunnerPropertiesFile, executionOutputPath);
    heimdallReporting.updateStatusInS3AndNotify();
  }

  /**
   * Generated the report from cucumber output path and push status to S3
   *
   * @param pathOfRunnerPropertiesFile pass the pathOfRunnerPropertiesFile
   * @throws IllegalAccessException illegalAccessException
   * @throws NoSuchFieldException   noSuchFieldException
   */
  public void updateStatusInS3(final String pathOfRunnerPropertiesFile,
      final String executionOutputPath)
      throws InterruptedException, IOException, NoSuchFieldException, IllegalAccessException {
    this.heimdallReporting = new HeimdallReporting(pathOfRunnerPropertiesFile, executionOutputPath);
    heimdallReporting.updateStatusInS3();
  }
}
