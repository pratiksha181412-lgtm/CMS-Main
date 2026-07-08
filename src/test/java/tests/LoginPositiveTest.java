package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.LoginPage;
import utils.ConfigReader;
import utils.ExcelTestCaseReader;
import utils.TestCase;

public class LoginPositiveTest extends BaseTest {

    private LoginPage loginPage;

    @BeforeMethod(alwaysRun = true)
    public void openLoginPage() {
        resetToLoginPage();
        loginPage = new LoginPage(page);
    }

    @Test(groups = "positive")
    public void tc02_loginPageLoadsWithEmailField() {
        TestCase testCase = ExcelTestCaseReader.findByTcId("Positive", "TC02")
                .orElseThrow(() -> new AssertionError("TC02 not found in Excel"));

        Assert.assertTrue(loginPage.isEmailFieldVisible(), testCase.getExpectedResults());
        Assert.assertTrue(loginPage.isGenerateOtpVisible(), "Generate OTP button should be visible");
    }

    @Test(groups = "positive")
    public void tc05_emailPlaceholderVisible() {
        TestCase testCase = ExcelTestCaseReader.findByTcId("Positive", "TC05")
                .orElseThrow(() -> new AssertionError("TC05 not found in Excel"));

        String placeholder = loginPage.getEmailPlaceholder();
        Assert.assertNotNull(placeholder, testCase.getExpectedResults());
        Assert.assertFalse(placeholder.isBlank(), "Email placeholder should not be blank");
    }

    @Test(groups = "positive")
    public void tc07_enterValidEmail() {
        TestCase testCase = ExcelTestCaseReader.findByTcId("Positive", "TC07")
                .orElseThrow(() -> new AssertionError("TC07 not found in Excel"));

        loginPage.fillEmail(ConfigReader.getProperty("email"));
        Assert.assertTrue(loginPage.isEmailFieldVisible(), testCase.getExpectedResults());
    }

    @Test(groups = "positive")
    public void tc08_generateOtpButtonVisible() {
        TestCase testCase = ExcelTestCaseReader.findByTcId("Positive", "TC08")
                .orElseThrow(() -> new AssertionError("TC08 not found in Excel"));

        Assert.assertTrue(loginPage.isGenerateOtpVisible(), testCase.getExpectedResults());
    }

    @Test(groups = "positive")
    public void tc14_otpSentAfterGenerateOtp() {
        TestCase testCase = ExcelTestCaseReader.findByTcId("Positive", "TC14")
                .orElseThrow(() -> new AssertionError("TC14 not found in Excel"));

        loginPage.fillEmail(ConfigReader.getProperty("email"));
        loginPage.clickGenerateOtp();

        Assert.assertTrue(
                loginPage.waitForOtpStep(10000) || loginPage.isOtpSentMessageVisible(),
                testCase.getExpectedResults());
    }
}
