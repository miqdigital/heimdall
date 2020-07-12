package com.miqdigital.services;

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

import com.miqdigital.dto.ExecutionInfoDto;
import com.miqdigital.dto.ScenarioInfoDto;
import com.miqdigital.dto.ScenarioStepDto;
import com.miqdigital.utils.ReadProperties;

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
  public ExecutionInfoDto getBuildExecutionDetails(final ReadProperties readProperties,
      final String executionOutputPath) throws IllegalAccessException, NoSuchFieldException {
    final List<TagObject> listOfTags = getListOfTags(executionOutputPath);
    LOGGER.info("Tags generated Successfully");
    return getBuildDetails(listOfTags, readProperties);
  }

  /**
   * Gets the list of all scenario tags.
   *
   * @param executionOutputPath pass cucumber result output path e.g target/cucumber-html-reports
   * @return lis of all cucumber tags
   * @throws NoSuchFieldException   noSuchFieldException
   * @throws IllegalAccessException illegalAccessException
   */
  private List<TagObject> getListOfTags(final String executionOutputPath)
      throws NoSuchFieldException, IllegalAccessException {
    final Collection<File> jsonFiles =
        FileUtils.listFiles(new File(executionOutputPath), new String[] {"json"}, true);
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
   * @param alltags list of all cucumber tags
   * @param projectJiraPrefix project jira prifix JA, JC
   * @return list of scenation info
   */
  private List<ScenarioInfoDto> parseAnnotations(final List<TagObject> alltags,
      final String projectJiraPrefix) {
    final List<ScenarioInfoDto> scenarioInfoDtoList = new LinkedList<>();
    for (final TagObject tag : alltags) {
      if (tag.getScenarios() >= 1) {
        final ScenarioInfoDto.ScenarioInfoDtoBuilder scenarioInfoBuilder =
            ScenarioInfoDto.builder().scenarioTagId(tag.getName())
                .scenarioTotalSteps(tag.getSteps())
                .scenarioTotalDuration(tag.getFormattedDuration());

        final net.masterthought.cucumber.json.Element element = tag.getElements().get(0);
        scenarioInfoBuilder.scenarioName(element.getName())
            .featureDescription(element.getFeature().getId())
            .featureFile(element.getFeature().getReportFileName())
            .scenarioStatus(tag.getStatus().toString());
        if (!tag.getStatus().isPassed()) {
          final ScenarioStepDto scenarioStepDto = parseFailedTag(tag);
          scenarioInfoBuilder.scenarioStepDto(scenarioStepDto);
        }
        final ScenarioInfoDto scenarioInfoDto = scenarioInfoBuilder.build();
        scenarioInfoDtoList.add(scenarioInfoDto);
      }
    }
    return scenarioInfoDtoList.stream()
        .filter(f -> f.getScenarioTagId().contains(projectJiraPrefix)).collect(Collectors.toList());
  }

  /**
   * Method to create ScenarioStepDto in case of scenario failure.
   *
   * @param tag Tagobject for which scenario failed
   * @return Scenario Step
   */
  private ScenarioStepDto parseFailedTag(final TagObject tag) {
    final ScenarioStepDto.ScenarioStepDtoBuilder scenarioStepBuilder = ScenarioStepDto.builder();
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
  private ExecutionInfoDto getBuildDetails(final List<TagObject> alltags,
      final ReadProperties readProperties) {
    final List<ScenarioInfoDto> scenarioInfoDtoList =
        parseAnnotations(alltags, readProperties.getJiraPrefix());
    final long passedTestCount =
        scenarioInfoDtoList.stream().filter(r -> r.getScenarioStatus().equals("PASSED")).count();
    final long failedTestCount =
        scenarioInfoDtoList.stream().filter(r -> r.getScenarioStatus().equals("FAILED")).count();

    return ExecutionInfoDto.builder().environment(System.getProperty("environment"))
        .testType(System.getProperty("tags")).dateTime(LocalDateTime.now().toString())
        .scenarioInfoDtoList(scenarioInfoDtoList).passTestCount(passedTestCount)
        .failTestCount(failedTestCount).totalTests(scenarioInfoDtoList.size())
        .BuildName(System.getProperty("JOB_NAME")).BuildNumber(System.getProperty("BUILD_NUMBER"))
        .build();
  }

}
