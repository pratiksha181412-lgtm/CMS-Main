package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class LoginPage {

    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    public void login(String email, String otp) {
        fillEmail(email);
        clickGenerateOtp();
        fillOtp(otp);
        submitLogin();
    }

    public void fillEmail(String email) {
        page.locator("input[type='email']")
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.locator("input[type='email']").fill(email);
    }

    public void clickGenerateOtp() {
        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Generate OTP"))
                .click();
        page.waitForTimeout(1500);
    }

    public void fillOtp(String otp) {
        Locator otpInputs = page.locator("input[type='tel']");
        otpInputs.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        for (int i = 0; i < otp.length(); i++) {
            otpInputs.nth(i).fill(String.valueOf(otp.charAt(i)));
        }
    }

    public void submitLogin() {
        Response loginResponse = page.waitForResponse(
                response -> response.url().contains("api/login"),
                new Page.WaitForResponseOptions().setTimeout(60000),
                () -> page.getByRole(
                        AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Submit"))
                        .click());

        String responseBody = loginResponse.text();
        if (!responseBody.contains("\"StatusCode\":1")) {
            throw new RuntimeException(
                    "Login API failed. Response: " + responseBody
                            + ". Visible errors: " + collectVisibleErrors());
        }

        page.waitForURL(
                url -> {
                    String lower = url.toLowerCase();
                    return lower.contains("/clients")
                            || lower.contains("/dashboard")
                            || lower.contains("/home");
                },
                new Page.WaitForURLOptions().setTimeout(60000));

        page.waitForLoadState();
    }

    public boolean isEmailFieldVisible() {
        return page.locator("input[type='email']").isVisible();
    }

    public boolean isGenerateOtpVisible() {
        return page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Generate OTP"))
                .isVisible();
    }

    public String getEmailPlaceholder() {
        return page.locator("input[type='email']").getAttribute("placeholder");
    }

    public boolean areOtpFieldsVisible() {
        return page.locator("input[type='tel']").count() >= 6
                && page.locator("input[type='tel']").first().isVisible();
    }

    public boolean waitForOtpStep(int timeoutMs) {
        try {
            page.locator("input[type='tel']")
                    .first()
                    .waitFor(new Locator.WaitForOptions().setTimeout(timeoutMs));
            return areOtpFieldsVisible();
        } catch (com.microsoft.playwright.PlaywrightException ignored) {
            return isOtpSentMessageVisible();
        }
    }

    public boolean isOtpSentMessageVisible() {
        String bodyText = page.locator("body").innerText().toLowerCase();
        return bodyText.matches("(?s).*(otp sent|otp has been sent|sent successfully|enter otp|verify otp).*");
    }

    public boolean isValidationMessageVisible() {
        return !collectVisibleErrors().isBlank();
    }

    public String getVisibleValidationMessage() {
        return collectVisibleErrors();
    }

    public String attemptLoginGetResponseBody(String email, String otp) {
        fillEmail(email);
        clickGenerateOtp();
        fillOtp(otp);

        Response loginResponse = page.waitForResponse(
                response -> response.url().contains("api/login"),
                new Page.WaitForResponseOptions().setTimeout(60000),
                () -> page.getByRole(
                        AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Submit"))
                        .click());

        page.waitForTimeout(1000);
        return loginResponse.text();
    }

    public boolean submitLoginExpectFailure() {
        try {
            Response loginResponse = page.waitForResponse(
                    response -> response.url().contains("api/login"),
                    new Page.WaitForResponseOptions().setTimeout(60000),
                    () -> page.getByRole(
                            AriaRole.BUTTON,
                            new Page.GetByRoleOptions().setName("Submit"))
                            .click());

            if (!loginResponse.text().contains("\"StatusCode\":1")) {
                return true;
            }
        } catch (com.microsoft.playwright.PlaywrightException ignored) {
            // Fall back to UI checks below.
        }

        page.waitForTimeout(1500);

        String url = page.url().toLowerCase();
        boolean stillOnLogin = url.endsWith("/") || url.contains("login");
        return stillOnLogin || isValidationMessageVisible();
    }

    public boolean isSubmitButtonVisible() {
        return page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Submit"))
                .isVisible();
    }

    public void loginExpectFailure(String email, String otp) {
        fillEmail(email);
        clickGenerateOtp();

        if (!otp.isBlank()) {
            fillOtp(otp);
        }

        if (!submitLoginExpectFailure()) {
            throw new AssertionError("Expected login to fail but user was redirected to: " + page.url());
        }
    }

    private String collectVisibleErrors() {
        return String.join(" | ", page.locator("p, span, div")
                .allInnerTexts()
                .stream()
                .map(String::trim)
                .filter(text -> !text.isBlank()
                        && text.length() < 120
                        && text.matches("(?i).*(invalid|error|otp|captcha|denied|failed|locked).*"))
                .distinct()
                .limit(5)
                .toList());
    }
}
