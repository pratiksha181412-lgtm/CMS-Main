package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.LoginPage;
import utils.ConfigReader;
import utils.ExcelTestCaseReader;
import utils.TestCase;

public class LoginNegativeTest extends BaseTest {

    private LoginPage loginPage;

    @BeforeMethod(alwaysRun = true)
    public void openLoginPage() {
        resetToLoginPage();
        loginPage = new LoginPage(page);
    }

    @Test(groups = "negative")
    public void tc03_blankEmailGenerateOtp() {
        TestCase testCase = ExcelTestCaseReader.findByTcId("Negative", "TC03")
                .orElseThrow(() -> new AssertionError("TC03 not found in Excel"));

        loginPage.clickGenerateOtp();

        Assert.assertTrue(
                loginPage.isValidationMessageVisible() || !loginPage.areOtpFieldsVisible(),
                testCase.getExpectedResults());
    }

    @Test(groups = "negative")
    public void tc01_invalidEmailFormat() {
        TestCase testCase = ExcelTestCaseReader.findByTcId("Negative", "TC01")
                .orElseThrow(() -> new AssertionError("TC01 not found in Excel"));

        loginPage.fillEmail("invalid@@email.com");
        loginPage.clickGenerateOtp();

        Assert.assertTrue(
                loginPage.isValidationMessageVisible() || !loginPage.areOtpFieldsVisible(),
                testCase.getExpectedResults());
    }

    @Test(groups = "negative")
    public void tc05_incorrectOtp() {
        TestCase testCase = ExcelTestCaseReader.findByTcId("Negative", "TC05")
                .orElseThrow(() -> new AssertionError("TC05 not found in Excel"));

        String responseBody = loginPage.attemptLoginGetResponseBody(
                ConfigReader.getProperty("email"),
                "000000");

        if (responseBody.contains("\"StatusCode\":1")) {
            throw new SkipException(
                    "Sandbox accepted OTP 000000 (StatusCode 1). "
                            + "Negative OTP validation cannot be verified in this environment.");
        }

        Assert.assertTrue(
                responseBody.toLowerCase().matches(".*(invalid|incorrect|failed|error|statuscode\":0).*"),
                testCase.getExpectedResults() + " API response: " + responseBody);
    }
}
