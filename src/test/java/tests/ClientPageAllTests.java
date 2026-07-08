package tests;

import base.AuthenticatedBaseTest;
import handlers.CaseResult;
import handlers.ClientCaseHandler;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.ClientPage;
import pages.DashboardPage;
import utils.ConfigReader;
import utils.ExcelTestCaseReader;
import utils.TestCase;

import java.util.ArrayList;
import java.util.List;

public class ClientPageAllTests extends AuthenticatedBaseTest {

    @DataProvider(name = "clientCases")
    public Object[][] clientCases() {
        List<TestCase> cases = new ArrayList<>();
        cases.addAll(ExcelTestCaseReader.clientCases("Positive"));
        cases.addAll(ExcelTestCaseReader.clientCases("Negative"));
        return cases.stream().map(tc -> new Object[] { tc }).toArray(Object[][]::new);
    }

    @BeforeMethod(alwaysRun = true)
    public void openClientsPage() {
        String baseUrl = ConfigReader.getProperty("url");
        new ClientPage(page).returnToClientsList(baseUrl);
        new DashboardPage(page).waitForDashboard();
    }

    @Test(dataProvider = "clientCases", groups = { "client", "all" })
    public void runClientExcelCase(TestCase testCase) {
        CaseResult result = ClientCaseHandler.run(page, testCase);
        if (result.skipped()) {
            throw new SkipException(testCase.getSheetName() + " " + testCase.getTcId() + ": " + result.message());
        }
        Assert.assertTrue(
                result.passed(),
                testCase.getSheetName() + " " + testCase.getTcId() + " failed: " + result.message()
                        + " | Expected: " + testCase.getExpectedResults());
    }
}
