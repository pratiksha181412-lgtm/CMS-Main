package utils;

public class TestCase {

    private final String sheetName;
    private final int rowNumber;
    private final String srNo;
    private final String testScenario;
    private final String tcId;
    private final String testType;
    private final String uiFunctional;
    private final String objective;
    private final String prerequisite;
    private final String testData;
    private final String steps;
    private final String expectedResults;

    public TestCase(
            String sheetName,
            int rowNumber,
            String srNo,
            String testScenario,
            String tcId,
            String testType,
            String uiFunctional,
            String objective,
            String prerequisite,
            String testData,
            String steps,
            String expectedResults) {
        this.sheetName = sheetName;
        this.rowNumber = rowNumber;
        this.srNo = srNo;
        this.testScenario = testScenario;
        this.tcId = tcId;
        this.testType = testType;
        this.uiFunctional = uiFunctional;
        this.objective = objective;
        this.prerequisite = prerequisite;
        this.testData = testData;
        this.steps = steps;
        this.expectedResults = expectedResults;
    }

    public String getSheetName() {
        return sheetName;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public String getSrNo() {
        return srNo;
    }

    public String getTestScenario() {
        return testScenario;
    }

    public String getTcId() {
        return tcId;
    }

    public String getTestType() {
        return testType;
    }

    public String getUiFunctional() {
        return uiFunctional;
    }

    public String getObjective() {
        return objective;
    }

    public String getPrerequisite() {
        return prerequisite;
    }

    public String getTestData() {
        return testData;
    }

    public String getSteps() {
        return steps;
    }

    public String getExpectedResults() {
        return expectedResults;
    }

    @Override
    public String toString() {
        return tcId + " - " + testScenario;
    }
}
