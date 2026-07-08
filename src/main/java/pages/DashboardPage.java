package pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;

public class DashboardPage {

    private final Page page;

    public DashboardPage(Page page) {
        this.page = page;
    }

    public boolean isDashboardVisible() {
        String url = page.url().toLowerCase();
        return url.contains("/clients")
                && !url.contains("/clients/add")
                && !url.contains("/clients/edit");
    }

    public void waitForDashboard() {
        page.waitForURL(
                url -> {
                    String lower = url.toLowerCase();
                    return lower.contains("/clients")
                            && !lower.contains("/clients/add")
                            && !lower.contains("/clients/edit");
                },
                new Page.WaitForURLOptions().setTimeout(60000));
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public void openRoles() {
        page.getByRole(
                AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Roles"))
                .click();
        page.waitForURL(
                url -> url.toLowerCase().contains("/roles"),
                new Page.WaitForURLOptions().setTimeout(60000));
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public void openClients() {
        page.getByRole(
                AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Clients"))
                .click();
        waitForDashboard();
    }

    public void clickAddClient() {
        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add Client"))
                .first()
                .click();
    }
}
