package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class LoginPage {

    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    public void login(String email, String otp) {
        page.locator("input[type='email']").fill(email);

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Generate OTP"))
                .click();

        Locator otpInputs = page.locator("input[type='tel']");
        otpInputs.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        for (int i = 0; i < otp.length(); i++) {
            otpInputs.nth(i).fill(String.valueOf(otp.charAt(i)));
        }

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Submit"))
                .click();

        page.waitForLoadState();
    }
}
