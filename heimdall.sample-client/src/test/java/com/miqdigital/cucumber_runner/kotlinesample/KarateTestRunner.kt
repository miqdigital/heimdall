package com.miqdigital.cucumber_runner.kotlinesample

import com.intuit.karate.cucumber.CucumberRunner
import com.miqdigital.Heimdall
import cucumber.api.CucumberOptions
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.io.IOException

/**
 * The type Karate test runner.
 */
@CucumberOptions(features = ["classpath:features/Karate"], tags = ["@regression"])
class KarateTestRunner {


    private val pathOfPropertyFile = ""
    private val cucumberOutputPath = ""

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

        val karateStats = CucumberRunner.parallel(javaClass, 5, cucumberOutputPath)

        val heimdall = Heimdall()
        heimdall.updateStatusInS3AndNotifySlack(pathOfPropertyFile, cucumberOutputPath)
        assertEquals("There are scenario failures", 0, karateStats.failCount.toLong())
    }
}