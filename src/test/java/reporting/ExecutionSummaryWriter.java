package reporting;

import utils.TestCase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class ExecutionSummaryWriter {

    private static final Path SUMMARY_PATH = Path.of("target", "execution-summary.txt");
    private static final Path EXECUTED_TESTCASES_PATH = Path.of("target", "executed-testcases.txt");

    private ExecutionSummaryWriter() {
    }

    public static void write(
            int total,
            int passed,
            int failed,
            int skipped,
            long durationMs,
            List<String> failureDetails) {
        try {
            Files.createDirectories(SUMMARY_PATH.getParent());
            List<String> lines = new ArrayList<>();
            lines.add("CMS Playwright Test Execution Summary");
            lines.add("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            lines.add("");
            lines.add("Total Executed : " + total);
            lines.add("Passed         : " + passed);
            lines.add("Failed         : " + failed);
            lines.add("Skipped        : " + skipped);
            lines.add("Duration (sec) : " + (durationMs / 1000.0));
            lines.add("");
            lines.add("Allure Report  : target/site/allure-maven-plugin/index.html");
            lines.add("TestNG Report  : target/surefire-reports/index.html");
            lines.add("Screenshots    : target/screenshots/");
            if (!failureDetails.isEmpty()) {
                lines.add("");
                lines.add("Failures:");
                lines.addAll(failureDetails);
            }
            Files.write(SUMMARY_PATH, lines, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // Summary file is best-effort.
        }
    }

    public static void writeExecutedTestcases(List<String> executedTestcases) {
        try {
            Files.createDirectories(EXECUTED_TESTCASES_PATH.getParent());
            Files.write(EXECUTED_TESTCASES_PATH, executedTestcases, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // Executed testcase list is best-effort.
        }
    }

    public static String formatTestCaseName(TestCase testCase) {
        return testCase.getSheetName() + " " + testCase.getTcId() + " - " + testCase.getTestScenario();
    }
}
