package com.miqdigital.runner.javasample;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.mail.MessagingException;

import org.junit.Test;

import com.intuit.karate.KarateOptions;
import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import com.miqdigital.Heimdall;

/**
 * The type Karate test runner.
 */
@KarateOptions
public class KarateTestRunner {


  private final String pathOfPropertyFile = "src/test/resources/properties/runner.properties";
  private final String karateOutputPath = "target/cucumber-reports";

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
      throws InterruptedException, NoSuchFieldException, IllegalAccessException, IOException,
      MessagingException {

    Results results =
        Runner.path("classpath:features/Karate").reportDir(karateOutputPath).tags("@regression")
            .parallel(10);

    final Heimdall heimdall = new Heimdall();
    heimdall.updateStatusInS3AndNotify(pathOfPropertyFile, karateOutputPath);
    assertEquals("There are scenario failures", 0, results.getFailCount());
  }
}
