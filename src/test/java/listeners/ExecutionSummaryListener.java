package listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import reporting.ExecutionSummaryWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExecutionSummaryListener implements ITestListener {
    private static final List<String> executedTestcases = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void onStart(ITestContext context) {
        executedTestcases.clear();
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        executedTestcases.add("[PASS] " + result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        executedTestcases.add("[FAIL] " + result.getName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        executedTestcases.add("[SKIP] " + result.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        int total = passed + failed + skipped;

        List<String> failureDetails = new ArrayList<>();
        for (ITestResult result : context.getFailedTests().getAllResults()) {
            Throwable throwable = result.getThrowable();
            failureDetails.add(result.getName() + " -> "
                    + (throwable == null ? "Unknown failure" : throwable.getMessage()));
        }

        long durationMs = context.getEndDate().getTime() - context.getStartDate().getTime();
        ExecutionSummaryWriter.write(total, passed, failed, skipped, durationMs, failureDetails);
        ExecutionSummaryWriter.writeExecutedTestcases(new ArrayList<>(executedTestcases));
    }
}
