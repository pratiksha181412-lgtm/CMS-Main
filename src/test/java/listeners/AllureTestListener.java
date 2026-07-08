package listeners;

import base.BaseTest;
import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AllureTestListener implements ITestListener {

    private static final Path SCREENSHOT_DIR = Path.of("target", "screenshots");

    @Override
    public void onTestFailure(ITestResult result) {
        attachFailureDetails(result);
        captureScreenshot(result);
    }

    private void attachFailureDetails(ITestResult result) {
        Throwable throwable = result.getThrowable();
        if (throwable == null) {
            return;
        }
        Allure.addAttachment(
                "Error Message",
                "text/plain",
                throwable.getMessage() == null ? "No message" : throwable.getMessage());
        Allure.addAttachment("Stack Trace", "text/plain", stackTrace(throwable));
    }

    private void captureScreenshot(ITestResult result) {
        Page page = resolvePage(result);
        if (page == null) {
            return;
        }
        try {
            Files.createDirectories(SCREENSHOT_DIR);
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
            String fileName = sanitize(result.getName()) + "_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    + ".png";
            Files.write(SCREENSHOT_DIR.resolve(fileName), screenshot);
            Allure.addAttachment("Failure Screenshot", "image/png", new ByteArrayInputStream(screenshot), ".png");
        } catch (Exception ignored) {
            // Screenshot is best-effort for reporting.
        }
    }

    private Page resolvePage(ITestResult result) {
        Object instance = result.getInstance();
        if (instance instanceof BaseTest baseTest) {
            return baseTest.getPage();
        }
        return null;
    }

    private String stackTrace(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        builder.append(throwable).append(System.lineSeparator());
        for (StackTraceElement element : throwable.getStackTrace()) {
            builder.append("  at ").append(element).append(System.lineSeparator());
        }
        return builder.toString();
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
