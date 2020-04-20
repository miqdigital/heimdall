package com.miqdigital.cucumber_runner.javasample;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.intuit.karate.cucumber.CucumberRunner;
import com.intuit.karate.cucumber.KarateStats;
import com.miqdigital.Heimdall;

import cucumber.api.CucumberOptions;

/**
 * The type Karate test runner.
 */
@CucumberOptions(features = {"classpath:features/Karate"},
                 tags = {"@regression"})
public class KarateTestRunner {


  private final String pathOfPropertyFile = "";
  private final String karateOutputPath = "";

  /**
   * Heimdall reporting.
   *
   * @throws InterruptedException   the interrupted exception
   * @throws NoSuchFieldException   the no such field exception
   * @throws IllegalAccessException the illegal access exception
   * @throws IOException            the io exception
   */
  @Test
  public void heimdallReporting()
      throws InterruptedException, NoSuchFieldException, IllegalAccessException, IOException {

    final KarateStats karateStats = CucumberRunner.parallel(getClass(), 5, karateOutputPath);

    final Heimdall heimdall = new Heimdall();
    heimdall.updateStatusInS3AndNotifySlack(pathOfPropertyFile, karateOutputPath);
    assertEquals("There are scenario failures", 0, karateStats.getFailCount());
  }
}
