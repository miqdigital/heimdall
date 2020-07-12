package com.miqdigital.runner.kotlinesample

import com.intuit.karate.KarateOptions
import com.intuit.karate.Results
import com.intuit.karate.Runner
import com.miqdigital.Heimdall
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

/**
 * The type Karate test runner.
 */
@KarateOptions(features = ["classpath:features/Karate"], tags = ["@regression"])
class KarateTestRunner {


  private val pathOfPropertyFile = "src/test/resources/properties/runner.properties"
  private val karateOutputPath = "target/cucumber-reports"

  /**
   * Heimdall reporting.
   *
   * @throws InterruptedException   the interrupted exception
   * @throws NoSuchFieldException   the no such field exception
   * @throws IllegalAccessException the illegal access exception
   * @throws IOException            the io exception
   */
  @Test
  @Throws(InterruptedException::class, NoSuchFieldException::class, IllegalAccessException::class, IOException::class)
  fun heimdallReporting() {

    val results: Results = Runner.path(karateOutputPath).tags("@regression").parallel(10)

    val heimdall = Heimdall()
    heimdall.updateStatusInS3AndNotify(pathOfPropertyFile, karateOutputPath)
    assertEquals("There are scenario failures", 0, results.failCount.toLong())
  }
}