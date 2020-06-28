package com.miqdigital.cucumber_runner;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.api.junit.Cucumber;

/**
 * The type Extended cucumber runner.
 */
public class ExtendedCucumberRunner extends Runner {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedCucumberRunner.class);
  private Class clazz;
  private Cucumber cucumber;


  /**
   * Instantiates a new Extended cucumber runner.
   *
   * @param clazzValue the clazz value
   * @throws InitializationError the exception
   */
  public ExtendedCucumberRunner(final Class clazzValue) throws InitializationError {
    clazz = clazzValue;
    cucumber = new Cucumber(clazzValue);
  }

  @Override
  public Description getDescription() {
    return cucumber.getDescription();
  }

  private void runPredefinedMethods(final Class annotation)
      throws InvocationTargetException, IllegalAccessException {
    if (!annotation.isAnnotation()) {
      return;
    }

    final Method[] methodList = this.clazz.getMethods();
    for (final Method method : methodList) {
      final Annotation methodAnnotation = method.getAnnotation(annotation);
      if (methodAnnotation != null) {
        method.invoke(null);
        break;
      }
    }
  }

  @Override
  public void run(final RunNotifier notifier) {
    try {
      runPredefinedMethods(BeforeSuite.class);
    } catch (final Exception e) {
      LOGGER.error(e.toString());
    }
    cucumber.run(notifier);
    try {
      runPredefinedMethods(AfterSuite.class);
    } catch (final Exception e) {
      LOGGER.error(e.toString());
    }
  }

}