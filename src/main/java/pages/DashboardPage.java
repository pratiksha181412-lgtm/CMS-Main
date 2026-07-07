package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class DashboardPage {

    private final Page page;

    public DashboardPage(Page page) {
        this.page = page;
    }

    public void openClients() {
        page.getByRole(
                AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Clients"))
                .click();
    }

    public void clickAddClient() {
        Locator addClientButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add Client"));

        if (addClientButton.count() == 0) {
            addClientButton = page.getByRole(
                    AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Client"));
        }

        addClientButton.first().click();
    }
}
