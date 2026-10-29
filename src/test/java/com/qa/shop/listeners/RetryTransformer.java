package com.qa.shop.listeners;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

/** Applies RetryAnalyzer to UI tests only — API assertions are deterministic and never retried. */
public class RetryTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        if (Arrays.asList(annotation.getGroups()).contains("ui")) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
        }
    }
}
