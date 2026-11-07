package com.student.linkedin.listeners;

import com.student.linkedin.tests.BaseTest;
import com.student.linkedin.utils.ScreenshotUtil;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        capture(result, "FAILED");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        capture(result, "PASSED");
    }

    private void capture(ITestResult result, String status) {
        Object instance = result.getInstance();
        if (instance instanceof BaseTest base && base.getDriver() != null) {
            String path = ScreenshotUtil.capture(base.getDriver(), result.getName() + "_" + status);
            System.out.println("Screenshot saved: " + path);
        }
    }
}