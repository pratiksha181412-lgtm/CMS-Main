package tests;

import base.BaseTest;
import handlers.CaseResult;
import handlers.TestCaseExecutor;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.ClientPage;
import pages.DashboardPage;
import pages.HomePage;
import pages.LoginPage;
import pages.RolesPage;
import reporting.ExecutionSummaryWriter;
import support.TestSupport;
import utils.ConfigReader;
import utils.ExcelTestCaseReader;
import utils.TestCase;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

@Epic("Planetums CMS")
@Feature("Excel Driven Regression")
public class FullSuiteTest extends BaseTest implements ITest {

    private static final String BASE_URL = ConfigReader.getProperty("url");
    private static boolean authenticated;
    private final ThreadLocal<String> testName = new ThreadLocal<>();

    @Test(priority = 1)
    @Description("Validates Positive and Negative Excel test case catalog loads successfully.")
    public void verifyExcelCatalogLoads() {
        if (TestCaseExecutor.isFilteredRun()) {
            throw new SkipException("Catalog check skipped for filtered testcase run.");
        }
        List<TestCase> positiveCases = ExcelTestCaseReader.readPositiveCases();
        List<TestCase> negativeCases = ExcelTestCaseReader.readNegativeCases();

        Assert.assertFalse(positiveCases.isEmpty(), "Positive sheet is empty.");
        Assert.assertFalse(negativeCases.isEmpty(), "Negative sheet is empty.");
        Allure.parameter("Positive Cases", positiveCases.size());
        Allure.parameter("Negative Cases", negativeCases.size());
    }

    @DataProvider(name = "allExcelCases")
    public Object[][] allExcelCases() {
        return TestCaseExecutor.selectedExcelCases().stream()
                .map(testCase -> new Object[] { testCase })
                .toArray(Object[][]::new);
    }

    @BeforeMethod(alwaysRun = true)
    public void prepareForTest(Method method, Object[] parameters) {
        if (parameters != null && parameters.length == 1 && parameters[0] instanceof TestCase testCase) {
            testName.set(ExecutionSummaryWriter.formatTestCaseName(testCase));
            System.out.println("Executing testcase: " + testName.get());
            prepareForModule(testCase);
            return;
        }
        testName.set(method.getName());
    }

    @Test(dataProvider = "allExcelCases", priority = 2)
    @Description("Executes each Excel test case exactly once in module order.")
    public void runExcelTestCase(TestCase testCase) {
        Allure.label("sheet", testCase.getSheetName());
        Allure.label("tcId", testCase.getTcId());
        Allure.label("module", TestCaseExecutor.moduleOf(testCase).name());
        Allure.parameter("Expected Result", testCase.getExpectedResults());

        CaseResult result = TestCaseExecutor.execute(page, testCase);
        if (result.skipped()) {
            throw new SkipException(result.message());
        }
        Assert.assertTrue(
                result.passed(),
                testCase.getSheetName() + " " + testCase.getTcId() + " failed: " + result.message()
                        + " | Expected: " + testCase.getExpectedResults());
    }

    @Test(priority = 3, dependsOnMethods = "runExcelTestCase", alwaysRun = true)
    @Feature("End-to-End Smoke")
    @Description("Smoke validation for client creation and role form workflow.")
    public void verifyEndToEndFlow() {
        if (TestCaseExecutor.isFilteredRun()) {
            throw new SkipException("E2E flow skipped for filtered testcase run.");
        }
        ensureAuthenticated();

        ClientPage clientPage = new ClientPage(page);
        clientPage.returnToClientsList(BASE_URL);
        clientPage.openAddClientForm(BASE_URL);
        Assert.assertTrue(clientPage.isAddClientFormVisible(), "Add Client form was not displayed.");

        String uniqueId = TestSupport.randomAlpha(10);
        clientPage.fillMandatoryClientForm(
                "Auto Test Client " + uniqueId,
                ConfigReader.getProperty("client.description"),
                ConfigReader.getProperty("client.officeAddress"),
                ConfigReader.getProperty("client.country"),
                ConfigReader.getProperty("client.timezone"),
                ConfigReader.getProperty("client.pocName"),
                ConfigReader.getProperty("client.pocEmail"),
                ConfigReader.getProperty("client.pocMobile"),
                "auto" + TestSupport.randomAlpha(8),
                ConfigReader.getProperty("client.licenses"),
                ConfigReader.getProperty("client.startDate"),
                ConfigReader.getProperty("client.endDate"),
                testImagePath(),
                testImagePath(),
                ConfigReader.getProperty("client.firstName"),
                ConfigReader.getProperty("client.lastName"),
                "autouser" + uniqueId + "@planetngtech.com",
                ConfigReader.getProperty("client.userRole"));

        clientPage.submitClientForm();

        DashboardPage dashboardPage = new DashboardPage(page);
        dashboardPage.waitForDashboard();
        Assert.assertTrue(dashboardPage.isDashboardVisible(), "Expected clients list after save.");

        dashboardPage.openRoles();
        RolesPage rolesPage = new RolesPage(page);
        rolesPage.waitForRolesPage();
        rolesPage.openAddRoleForm();
        rolesPage.fillAddRoleForm(
                ConfigReader.getProperty("role.name"),
                ConfigReader.getProperty("role.description"),
                ConfigReader.getProperty("role.type"));
        Assert.assertTrue(rolesPage.areAllSection2CheckboxesSelected(), "Section 2 checkboxes not all selected.");
    }

    @Override
    public String getTestName() {
        return testName.get() == null ? "FullSuiteTest" : testName.get();
    }

    private void prepareForModule(TestCase testCase) {
        switch (TestCaseExecutor.moduleOf(testCase)) {
            case LOGIN -> resetToLoginPage();
            case CLIENT -> {
                ensureAuthenticated();
                new ClientPage(page).returnToClientsList(BASE_URL);
                new DashboardPage(page).waitForDashboard();
            }
            case ROLES -> {
                ensureAuthenticated();
                RolesPage rolesPage = new RolesPage(page);
                rolesPage.returnToRolesList(BASE_URL);
                rolesPage.waitForRolesPage();
            }
            case OTHER -> ensureAuthenticated();
            default -> {
            }
        }
    }

    private void ensureAuthenticated() {
        if (authenticated) {
            return;
        }
        HomePage homePage = new HomePage(page);
        if (!homePage.isUserLoggedIn()) {
            resetToLoginPage();
            new LoginPage(page).login(ConfigReader.getProperty("email"), ConfigReader.getProperty("otp"));
        }
        Assert.assertTrue(homePage.isUserLoggedIn(), "Authentication failed.");
        authenticated = true;
    }

    private Path testImagePath() {
        return TestSupport.testImagePath(getClass());
    }
}
