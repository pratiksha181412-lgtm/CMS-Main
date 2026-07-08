package handlers;

import com.microsoft.playwright.Page;
import utils.ExcelTestCaseReader;
import utils.TestCase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class TestCaseExecutor {
    private static final String TC_ID_FILTER = System.getProperty("tcId", "").trim();
    private static final String SHEET_FILTER = System.getProperty("sheet", "").trim();

    public enum Module {
        LOGIN(1),
        CLIENT(2),
        ROLES(3),
        OTHER(4);

        private final int order;

        Module(int order) {
            this.order = order;
        }

        public int order() {
            return order;
        }
    }

    private TestCaseExecutor() {
    }

    public static List<TestCase> allExcelCasesOnce() {
        List<TestCase> cases = new ArrayList<>();
        cases.addAll(ExcelTestCaseReader.readPositiveCases());
        cases.addAll(ExcelTestCaseReader.readNegativeCases());
        cases.sort(Comparator
                .comparingInt((TestCase tc) -> moduleOf(tc).order())
                .thenComparing(tc -> tc.getSheetName())
                .thenComparing(tc -> tc.getTcId()));
        return List.copyOf(cases);
    }

    public static List<TestCase> selectedExcelCases() {
        List<TestCase> selected = allExcelCasesOnce().stream()
                .filter(testCase -> matchesTcId(testCase) && matchesSheet(testCase))
                .toList();

        if (!TC_ID_FILTER.isBlank() && SHEET_FILTER.isBlank()) {
            long distinctSheets = selected.stream()
                    .map(TestCase::getSheetName)
                    .distinct()
                    .count();
            if (distinctSheets > 1) {
                throw new IllegalArgumentException(
                        "Multiple test cases match tcId '" + TC_ID_FILTER
                                + "'. Add -Dsheet=Positive or -Dsheet=Negative.");
            }
        }

        if ((!TC_ID_FILTER.isBlank() || !SHEET_FILTER.isBlank()) && selected.isEmpty()) {
            throw new IllegalArgumentException(
                    "No test cases matched tcId='" + TC_ID_FILTER + "' and sheet='" + SHEET_FILTER + "'.");
        }

        return selected;
    }

    public static boolean isFilteredRun() {
        return !TC_ID_FILTER.isBlank() || !SHEET_FILTER.isBlank();
    }

    public static Module moduleOf(TestCase testCase) {
        String scenario = testCase.getTestScenario().toLowerCase(Locale.ROOT);
        if (scenario.contains("cms login")) {
            return Module.LOGIN;
        }
        if (scenario.contains("client management")
                || scenario.contains("license configuration")
                || scenario.contains("company-specific login page branding")) {
            return Module.CLIENT;
        }
        if (scenario.contains("role name")
                || scenario.contains("role type")
                || scenario.contains("role creation")
                || scenario.contains("associated activities")) {
            return Module.ROLES;
        }
        return Module.OTHER;
    }

    public static CaseResult execute(Page page, TestCase testCase) {
        return switch (moduleOf(testCase)) {
            case LOGIN -> LoginCaseHandler.run(page, testCase);
            case CLIENT -> ClientCaseHandler.run(page, testCase);
            case ROLES -> RolesCaseHandler.run(page, testCase);
            case OTHER -> CaseResult.skip("Module not automated yet: " + testCase.getTestScenario());
        };
    }

    private static boolean matchesTcId(TestCase testCase) {
        return TC_ID_FILTER.isBlank() || testCase.getTcId().equalsIgnoreCase(TC_ID_FILTER);
    }

    private static boolean matchesSheet(TestCase testCase) {
        return SHEET_FILTER.isBlank() || testCase.getSheetName().equalsIgnoreCase(SHEET_FILTER);
    }
}
