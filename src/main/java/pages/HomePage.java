package pages;

import com.microsoft.playwright.Page;

public class HomePage {

    private final Page page;

    public HomePage(Page page) {
        this.page = page;
    }

    public String getPageTitle() {
        return page.title();
    }

    public String getCurrentUrl() {
        return page.url();
    }

    public boolean isUserLoggedIn() {
        String url = page.url().toLowerCase();

        if (url.contains("login") || url.endsWith("/")) {
            return false;
        }

        return url.contains("dashboard")
                || url.contains("home")
                || url.contains("cms")
                || url.contains("clients")
                || page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Add Client")).count() > 0
                || page.getByRole(com.microsoft.playwright.options.AriaRole.LINK,
                        new Page.GetByRoleOptions().setName("Clients")).count() > 0;
    }
}
