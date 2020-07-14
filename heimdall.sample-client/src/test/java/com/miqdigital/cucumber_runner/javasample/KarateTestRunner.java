package com.miqdigital.cucumber_runner.javasample;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.intuit.karate.KarateOptions;
import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import com.miqdigital.Heimdall;

/**
 * The type Karate test runner.
 */
@KarateOptions(features = {"classpath:features/Karate"},
               tags = {"@regression"})
public class KarateTestRunner {


  private final String pathOfPropertyFile = "";
  private final String karateOutputPath = "";

  /**
   * Heimdall reporting.
   *
   * @throws InterruptedException the interrupted exception
   * @throws NoSuchFieldException the no such field exception
   * @throws IllegalAccessException the illegal access exception
   * @throws IOException the io exception
   */
  @Test
  public void heimdallReporting()
      throws InterruptedException, NoSuchFieldException, IllegalAccessException, IOException {

    Results results = Runner.path(karateOutputPath).tags("@regression").parallel(10);

    final Heimdall heimdall = new Heimdall();
    heimdall.updateStatusInS3AndNotifySlack(pathOfPropertyFile, karateOutputPath);
    assertEquals("There are scenario failures", 0, results.getFailCount());
  }
}
