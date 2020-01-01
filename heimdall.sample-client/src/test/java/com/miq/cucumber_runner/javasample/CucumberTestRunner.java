package com.miq.cucumber_runner.javasample;

import java.io.IOException;

import org.junit.runner.RunWith;

import com.miq.Heimdall;
import com.miq.cucumber_runner.ExtendedCucumberRunner;

import cucumber.api.CucumberOptions;

/**
 * RunnerTest for all the test cases.
 */
@RunWith(ExtendedCucumberRunner.class)
@CucumberOptions(features = "classpath:features/Cucumber",
                 glue = "classpath:com/miq" + "/step_definition",
                 tags = {"@sanity"},
                 plugin = {"html:target/cucumber-html-report",
                           "json:target/cucumber-reports/Cucumber.json"},
                 monochrome = true)

public class CucumberTestRunner {

  private final String cucumberOutputPath = "";
  private final String pathOfPropertyFile = "";

  /**
   * Heimdall reporting.
   *
   * @throws IllegalAccessException the illegal access exception
   * @throws NoSuchFieldException   the no such field exception
   * @throws InterruptedException   the interrupted exception
   * @throws IOException            the io exception
   */
  public void heimdallReporting()
      throws IllegalAccessException, NoSuchFieldException, InterruptedException, IOException {
    final Heimdall heimdall = new Heimdall();
    heimdall.updateStatusInS3AndNotifySlack(pathOfPropertyFile, cucumberOutputPath);
  }
}
