package com.miqdigital.execution;

import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miqdigital.execution.dto.ExecutionInfo;
import com.miqdigital.scenario.dto.ScenarioInfo;
import com.miqdigital.scenario.dto.ScenarioStep;
import com.miqdigital.utility.ReadProperties;

import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import net.masterthought.cucumber.ReportResult;
import net.masterthought.cucumber.json.support.TagObject;

/**
 * This class generates the execution info of the test scenarios.
 */
public class ExecutionInfoGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionInfoGenerator.class);

  /**
   * Gets the builds execution details of the test run.
   *
   * @param readProperties
   * @param executionOutputPath
   * @return
   * @throws IllegalAccessException
   * @throws NoSuchFieldException
   */
  public ExecutionInfo getBuildExecutionDetails(final ReadProperties readProperties,
      final String executionOutputPath) throws IllegalAccessException, NoSuchFieldException {
    final List<TagObject> listOfTags = getListOfTags(executionOutputPath);
    LOGGER.info("Tags generated Successfully");
    return getBuildDetails(listOfTags, readProperties);
  }

  /**
   * Gets the list of all scenario tags.
   *
   * @param cucumberOutputPath pass cucumber result output path e.g target/cucumber-html-reports
   * @return lis of all cucumber tags
   * @throws NoSuchFieldException   noSuchFieldException
   * @throws IllegalAccessException illegalAccessException
   */
  private List<TagObject> getListOfTags(final String cucumberOutputPath)
      throws NoSuchFieldException, IllegalAccessException {
    final Collection<File> jsonFiles =
        FileUtils.listFiles(new File(cucumberOutputPath), new String[] {"json"}, true);
    final List<String> jsonPaths = new ArrayList<>(jsonFiles.size());
    jsonFiles.forEach(file -> jsonPaths.add(file.getAbsolutePath()));
    final Configuration config = new Configuration(new File("target"), "demo");
    final ReportBuilder reportBuilder = new ReportBuilder(jsonPaths, config);
    reportBuilder.generateReports();
    final Field reportResult = reportBuilder.getClass().getDeclaredField("reportResult");
    reportResult.setAccessible(true);
    final ReportResult result = (ReportResult) reportResult.get(reportBuilder);
    return result.getAllTags();
  }


  /**
   * Method to parse cucumber annotation no project level.
   *
   * @param alltags           list of all cucumber tags
   * @param projectJiraPrefix project jira prifix JA, JC
   * @return list of scenation info
   */
  private List<ScenarioInfo> parseAnnotations(final List<TagObject> alltags,
      final String projectJiraPrefix) {
    final List<ScenarioInfo> scenarioInfoList = new LinkedList<>();
    for (final TagObject tag : alltags) {
      LOGGER.info("Parsing tags: {}", tag.getName());
      if (tag.getScenarios() >= 1) {
        final ScenarioInfo.ScenarioInfoBuilder scenarioInfoBuilder =
            ScenarioInfo.builder().scenarioTagId(tag.getName()).scenarioTotalSteps(tag.getSteps())
                .scenarioTotalDuration(tag.getFormattedDuration());

        final net.masterthought.cucumber.json.Element element = tag.getElements().get(0);
        scenarioInfoBuilder.scenarioName(element.getName())
            .featureDescription(element.getFeature().getId())
            .featureFile(element.getFeature().getReportFileName())
            .scenarioStatus(tag.getStatus().toString());
        if (!tag.getStatus().isPassed()) {
          final ScenarioStep scenarioStep = parseFailedTag(tag);
          scenarioInfoBuilder.scenarioStep(scenarioStep);
        }
        final ScenarioInfo scenarioInfo = scenarioInfoBuilder.build();
        scenarioInfoList.add(scenarioInfo);
      }
    }
    return scenarioInfoList.stream().filter(f -> f.getScenarioTagId().contains(projectJiraPrefix))
        .collect(Collectors.toList());
  }

  /**
   * Method to create ScenarioStep in case of scenario failure.
   *
   * @param tag Tagobject for which scenario failed
   * @return Scenario Step
   */
  private ScenarioStep parseFailedTag(final TagObject tag) {
    final ScenarioStep.ScenarioStepBuilder scenarioStepBuilder = ScenarioStep.builder();
    Arrays.stream(tag.getElements().get(0).getSteps())
        .filter(s -> !s.getResult().getStatus().isPassed()).peek(
        f -> scenarioStepBuilder.errMessage(f.getResult().getErrorMessage())
            .scenarioLine(f.getName()).stepDuration(f.getResult().getFormattedDuration()))
        .collect(Collectors.toList());
    return scenarioStepBuilder.build();
  }

  /**
   * Get build status of all the test scenarios.
   *
   * @param alltags        List of all the tags present in a feature file
   * @param readProperties readProperties
   */
  private ExecutionInfo getBuildDetails(final List<TagObject> alltags,
      final ReadProperties readProperties) {
    final List<ScenarioInfo> scenarioInfoList =
        parseAnnotations(alltags, readProperties.getJiraPrefix());
    final long passedTestCount =
        scenarioInfoList.stream().filter(r -> r.getScenarioStatus().equals("PASSED")).count();
    final long failedTestCount =
        scenarioInfoList.stream().filter(r -> r.getScenarioStatus().equals("FAILED")).count();

    return ExecutionInfo.builder().environment(System.getProperty("environment"))
        .testType(System.getProperty("tags")).dateTime(LocalDateTime.now().toString())
        .scenarioInfoList(scenarioInfoList).passTestCount(passedTestCount)
        .failTestCount(failedTestCount).totalTests(scenarioInfoList.size())
        .BuildName(System.getProperty("JOB_NAME")).BuildNumber(System.getProperty("BUILD_NUMBER"))
        .build();
  }

}
