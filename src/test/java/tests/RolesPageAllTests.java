package tests;

import base.AuthenticatedBaseTest;
import handlers.CaseResult;
import handlers.RolesCaseHandler;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.RolesPage;
import utils.ConfigReader;
import utils.ExcelTestCaseReader;
import utils.TestCase;

import java.util.ArrayList;
import java.util.List;

public class RolesPageAllTests extends AuthenticatedBaseTest {

    @DataProvider(name = "rolesCases")
    public Object[][] rolesCases() {
        List<TestCase> cases = new ArrayList<>();
        cases.addAll(ExcelTestCaseReader.rolesCases("Positive"));
        cases.addAll(ExcelTestCaseReader.rolesCases("Negative"));
        return cases.stream().map(tc -> new Object[] { tc }).toArray(Object[][]::new);
    }

    @BeforeMethod(alwaysRun = true)
    public void openRolesPage() {
        RolesPage rolesPage = new RolesPage(page);
        rolesPage.returnToRolesList(ConfigReader.getProperty("url"));
        rolesPage.waitForRolesPage();
    }

    @Test(dataProvider = "rolesCases", groups = { "roles", "all" })
    public void runRolesExcelCase(TestCase testCase) {
        CaseResult result = RolesCaseHandler.run(page, testCase);
        if (result.skipped()) {
            throw new SkipException(testCase.getSheetName() + " " + testCase.getTcId() + ": " + result.message());
        }
        Assert.assertTrue(
                result.passed(),
                testCase.getSheetName() + " " + testCase.getTcId() + " failed: " + result.message()
                        + " | Expected: " + testCase.getExpectedResults());
    }
}
