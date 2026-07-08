package tests;

import base.BaseTest;
import handlers.CaseResult;
import handlers.LoginCaseHandler;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.ExcelTestCaseReader;
import utils.TestCase;

import java.util.ArrayList;
import java.util.List;

public class LoginPageAllTests extends BaseTest {

    @DataProvider(name = "loginCases")
    public Object[][] loginCases() {
        List<TestCase> cases = new ArrayList<>();
        cases.addAll(ExcelTestCaseReader.loginCases("Positive"));
        cases.addAll(ExcelTestCaseReader.loginCases("Negative"));
        return cases.stream().map(tc -> new Object[] { tc }).toArray(Object[][]::new);
    }

    @BeforeMethod(alwaysRun = true)
    public void openLoginPage() {
        resetToLoginPage();
    }

    @Test(dataProvider = "loginCases", groups = { "login", "all" })
    public void runLoginExcelCase(TestCase testCase) {
        CaseResult result = LoginCaseHandler.run(page, testCase);
        if (result.skipped()) {
            throw new SkipException(testCase.getSheetName() + " " + testCase.getTcId() + ": " + result.message());
        }
        Assert.assertTrue(
                result.passed(),
                testCase.getSheetName() + " " + testCase.getTcId() + " failed: " + result.message()
                        + " | Expected: " + testCase.getExpectedResults());
    }
}
