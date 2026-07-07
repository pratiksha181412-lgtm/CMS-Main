package pages;

import com.microsoft.playwright.FileChooser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.nio.file.Path;

public class ClientPage {

    private final Page page;

    public ClientPage(Page page) {
        this.page = page;
    }

    public void open(String baseUrl, String clientPagePath) {
        String path = (clientPagePath == null || clientPagePath.isBlank()) ? "/clients" : clientPagePath;
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        page.navigate(normalizedBase + path);
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public void clickAddClient() {
        if (page.url().contains("/clients/add")) {
            return;
        }

        Locator addClientButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add Client"));

        if (addClientButton.count() == 0) {
            addClientButton = page.getByRole(
                    AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Client"));
        }

        addClientButton.first().click();
        waitForAddClientForm();
    }

    public void openAddClientForm(String baseUrl) {
        page.waitForResponse(
                response -> response.url().contains("GetAllDetailsToAddclient"),
                new Page.WaitForResponseOptions().setTimeout(60000),
                () -> open(baseUrl, "/clients/add"));
        waitForAddClientForm();
    }

    public void waitForAddClientForm() {
        page.getByPlaceholder("Enter client name")
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public boolean isAddClientFormVisible() {
        return page.getByPlaceholder("Enter client name").isVisible();
    }

    public void enterClientName(String name) {
        page.getByPlaceholder("Enter client name").fill(name);
    }

    public void enterDescription(String description) {
        page.getByPlaceholder("Enter description").fill(description);
    }

    public void enterOfficeAddress(String address) {
        page.getByPlaceholder("Enter office address").fill(address);
    }

    public void selectCountry(String country) {
        selectFromDropdown(0, country);
    }

    public void selectTimezone(String timezone) {
        selectFromDropdown(0, timezone);
    }

    public void enterPOCName(String pocName) {
        page.getByPlaceholder("Enter POC name").fill(pocName);
    }

    public void enterPOCEmail(String email) {
        page.getByPlaceholder("Enter POC email").fill(email);
    }

    public void enterPOCMobile(String mobile) {
        page.getByPlaceholder("Enter POC mobile number").fill(mobile);
    }

    public void enterBrandingURL(String url) {
        page.getByPlaceholder("globaltechsolutions").fill(url);
    }

    public void uploadLoginPageImage(Path imagePath) {
        uploadImage(0, imagePath);
    }

    public void uploadCompanyLogo(Path imagePath) {
        uploadImage(1, imagePath);
    }

    public void enterLicenses(String licenses) {
        page.getByPlaceholder("Enter number of licenses").fill(licenses);
    }

    public void enterStartDate(String startDate) {
        page.locator("input[type='date']").first().fill(startDate);
    }

    public void enterEndDate(String endDate) {
        page.getByPlaceholder("End Date").fill(endDate);
    }

    public void enterFirstName(String firstName) {
        page.getByPlaceholder("Enter first name").fill(firstName);
    }

    public void enterLastName(String lastName) {
        page.getByPlaceholder("Enter last name").fill(lastName);
    }

    public void enterUserEmail(String email) {
        page.getByPlaceholder("Enter email id (@planetngtech.com)").fill(email);
    }

    public void selectUserRole(String role) {
        page.locator("div")
                .filter(new Locator.FilterOptions().setHasText("Section 4: System Users"))
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("- Select -"))
                .first()
                .click();
        page.getByText(role, new Page.GetByTextOptions().setExact(true))
                .first()
                .click();
        page.waitForTimeout(300);
    }

    public void fillMandatoryClientForm(
            String clientName,
            String description,
            String officeAddress,
            String country,
            String timezone,
            String pocName,
            String pocEmail,
            String pocMobile,
            String brandingUrl,
            String licenses,
            String startDate,
            String endDate,
            Path loginPageImage,
            Path companyLogo,
            String firstName,
            String lastName,
            String userEmail,
            String userRole) {

        enterClientName(clientName);
        enterDescription(description);
        enterOfficeAddress(officeAddress);
        selectCountry(country);
        selectTimezone(timezone);
        enterPOCName(pocName);
        enterPOCEmail(pocEmail);
        enterPOCMobile(pocMobile);
        enterBrandingURL(brandingUrl);
        uploadLoginPageImage(loginPageImage);
        uploadCompanyLogo(companyLogo);
        enterLicenses(licenses);
        enterStartDate(startDate);
        enterEndDate(endDate);
        enterFirstName(firstName);
        enterLastName(lastName);
        enterUserEmail(userEmail);
        selectUserRole(userRole);
    }

    public void submitClientForm() {
        Locator saveButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Save"))
                .first();
        saveButton.scrollIntoViewIfNeeded();

        page.waitForResponse(
                response -> response.url().contains("AddOrEditClient"),
                new Page.WaitForResponseOptions().setTimeout(60000),
                saveButton::click);

        page.waitForURL(
                url -> isPostSaveUrl(url),
                new Page.WaitForURLOptions().setTimeout(60000));

        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public boolean isSaveSuccessful() {
        return isPostSaveUrl(page.url());
    }

    public boolean isClientListed(String clientName) {
        if (!isPostSaveUrl(page.url())) {
            return false;
        }

        try {
            Locator clientRow = page.getByText(clientName, new Page.GetByTextOptions().setExact(true));
            clientRow.first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
            return clientRow.first().isVisible();
        } catch (com.microsoft.playwright.PlaywrightException ex) {
            return false;
        }
    }

    private void uploadImage(int uploadButtonIndex, Path imagePath) {
        Locator uploadButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Upload")).nth(uploadButtonIndex);

        FileChooser fileChooser = page.waitForFileChooser(uploadButton::click);
        fileChooser.setFiles(imagePath);
        page.waitForTimeout(1500);
    }

    private boolean isPostSaveUrl(String url) {
        String lower = url.toLowerCase();
        return !lower.contains("/clients/add")
                && (lower.contains("/dashboard") || lower.contains("/clients") || lower.contains("/home"));
    }

    private void selectFromDropdown(int index, String option) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("- Select -"))
                .nth(index)
                .click();
        page.getByText(option, new Page.GetByTextOptions().setExact(true))
                .first()
                .click();
        page.waitForTimeout(300);
    }
}
