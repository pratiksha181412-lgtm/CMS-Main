package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

public class RolesPage {

    private final Page page;

    public RolesPage(Page page) {
        this.page = page;
    }

    public boolean isRolesPageVisible() {
        String url = page.url().toLowerCase();
        return url.contains("/roles") && !url.contains("/roles/add") && !url.contains("/roles/edit");
    }

    public void waitForRolesPage() {
        page.waitForURL(
                url -> url.toLowerCase().contains("/roles")
                        && !url.toLowerCase().contains("/roles/add")
                        && !url.toLowerCase().contains("/roles/edit"),
                new Page.WaitForURLOptions().setTimeout(60000));
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public void clickAddRole() {
        Locator addRoleButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add Role"));

        if (addRoleButton.count() == 0) {
            addRoleButton = page.locator("button.btn-add");
        }

        addRoleButton.first().click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public void openAddRoleForm() {
        if (page.url().contains("/roles/add") && isAddRoleFormVisible()) {
            return;
        }

        try {
            page.waitForResponse(
                    response -> response.url().contains("GetAllDetailsToAddRole"),
                    new Page.WaitForResponseOptions().setTimeout(15000),
                    this::clickAddRole);
        } catch (com.microsoft.playwright.PlaywrightException ignored) {
            clickAddRole();
        }

        waitForAddRoleForm();
        try {
            waitForActivitiesLoaded();
        } catch (com.microsoft.playwright.PlaywrightException ignored) {
            // Activities may already be loaded.
        }
    }

    public void returnToRolesList(String baseUrl) {
        if (isRolesPageVisible()) {
            return;
        }
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        page.navigate(normalizedBase + "/roles");
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public void fillAddRoleForm(String name, String description, String type) {
        fillRoleName(name);
        page.getByPlaceholder("Enter description").fill(description);
        selectType(type);
        selectAllSection2Checkboxes();
    }

    public void fillRoleName(String name) {
        page.getByPlaceholder("Enter role name").fill(name);
        page.keyboard().press("Tab");
        page.waitForTimeout(300);
    }

    public String getRoleNameValue() {
        return page.getByPlaceholder("Enter role name").inputValue();
    }

    public boolean isRoleNameFieldVisible() {
        return page.getByPlaceholder("Enter role name").isVisible();
    }

    public boolean isRoleTypeDropdownVisible() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("- Select -")).count() > 0;
    }

    public boolean openRoleTypeDropdown() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("- Select -"))
                .first()
                .click();
        page.waitForTimeout(500);
        return page.getByText("System", new Page.GetByTextOptions().setExact(true)).count() > 0;
    }

    public void selectRoleType(String type) {
        selectType(type);
    }

    public boolean hasRoleTypeOptions(String... options) {
        openRoleTypeDropdown();
        for (String option : options) {
            if (page.getByText(option, new Page.GetByTextOptions().setExact(true)).count() == 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isAssociatedActivitiesGridVisible() {
        String body = page.locator("body").innerText().toLowerCase();
        return (body.contains("associated activities") || body.contains("section 2"))
                && section2Checkboxes().count() > 0;
    }

    public int getActivityCheckboxCount() {
        return section2Checkboxes().count();
    }

    public boolean hasActivityGridData() {
        return section2Checkboxes().count() > 0
                && page.locator("tbody tr").count() > 0;
    }

    public boolean hasSystemActivityTag() {
        return page.locator("body").innerText().toLowerCase().contains("system");
    }

    public boolean hasValidationError() {
        String body = page.locator("body").innerText().toLowerCase();
        return body.matches("(?s).*(required|invalid|must|cannot|please enter|error|mandatory|special).*");
    }

    public void clickSaveWithoutConfirm() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"))
                .first()
                .scrollIntoViewIfNeeded();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"))
                .first()
                .click();
        page.waitForTimeout(1000);
    }

    public boolean areAllSection2CheckboxesSelected() {
        Locator checkboxes = section2Checkboxes();
        int count = checkboxes.count();
        if (count == 0) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            if (!checkboxes.nth(i).isChecked()) {
                return false;
            }
        }
        return true;
    }

    public void waitForAddRoleForm() {
        page.waitForURL(
                url -> url.toLowerCase().contains("/roles/add"),
                new Page.WaitForURLOptions().setTimeout(60000));
        page.getByPlaceholder("Enter role name")
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public boolean isAddRoleFormVisible() {
        return page.url().contains("/roles/add")
                && page.getByPlaceholder("Enter role name").isVisible();
    }

    private void waitForActivitiesLoaded() {
        section2Checkboxes().first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
    }

    private Locator section2Checkboxes() {
        return page.locator("div")
                .filter(new Locator.FilterOptions().setHasText("Section 2: Associated Activities"))
                .locator("tbody input[type='checkbox']");
    }

    private void selectType(String type) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("- Select -"))
                .first()
                .click();
        page.getByText(type, new Page.GetByTextOptions().setExact(true))
                .first()
                .click();
        page.waitForTimeout(500);
    }

    private void selectAllSection2Checkboxes() {
        Locator checkboxes = section2Checkboxes();
        int count = checkboxes.count();
        for (int i = 0; i < count; i++) {
            Locator checkbox = checkboxes.nth(i);
            if (!checkbox.isChecked()) {
                checkbox.check();
            }
        }
    }
}
