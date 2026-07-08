package pages;

import com.microsoft.playwright.FileChooser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.nio.file.Path;

public class ClientPage {

    private final Page page;

    public ClientPage(Page page) {
        this.page = page;
    }

    public void openAddClientForm(String baseUrl) {
        if (page.url().contains("/clients/add") && isAddClientFormVisible()) {
            return;
        }

        if (!page.url().contains("/clients") || page.url().contains("/clients/add")) {
            String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            page.navigate(normalizedBase + "/clients");
            page.waitForLoadState(LoadState.NETWORKIDLE);
        }

        try {
            page.waitForResponse(
                    response -> response.url().contains("GetAllDetailsToAddclient"),
                    new Page.WaitForResponseOptions().setTimeout(15000),
                    this::clickAddClient);
        } catch (com.microsoft.playwright.PlaywrightException ignored) {
            clickAddClient();
        }

        waitForAddClientForm();
    }

    public void returnToClientsList(String baseUrl) {
        if (page.url().contains("/clients") && !page.url().contains("/clients/add")) {
            return;
        }
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        page.navigate(normalizedBase + "/clients");
        page.waitForLoadState(LoadState.NETWORKIDLE);
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
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public void waitForAddClientForm() {
        page.getByPlaceholder("Enter client name")
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public boolean isAddClientFormVisible() {
        return page.getByPlaceholder("Enter client name").isVisible();
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

        page.getByPlaceholder("Enter client name").fill(clientName);
        page.getByPlaceholder("Enter description").fill(description);
        page.getByPlaceholder("Enter office address").fill(officeAddress);

        selectFromDropdown(0, country);
        selectFromDropdown(0, timezone);

        page.getByPlaceholder("Enter POC name").fill(pocName);
        page.getByPlaceholder("Enter POC email").fill(pocEmail);
        page.getByPlaceholder("Enter POC mobile number").fill(pocMobile);
        page.getByPlaceholder("globaltechsolutions").fill(brandingUrl);
        page.keyboard().press("Tab");
        page.waitForTimeout(1000);

        attachImage(0, loginPageImage);
        attachImage(1, companyLogo);

        page.getByPlaceholder("Enter number of licenses").fill(licenses);
        page.getByPlaceholder("Enter number", new Page.GetByPlaceholderOptions().setExact(true)).fill("2");
        page.locator("input[type='date']").first().fill(startDate);
        page.getByPlaceholder("End Date").fill(endDate);

        page.getByPlaceholder("Enter first name").fill(firstName);
        page.getByPlaceholder("Enter last name").fill(lastName);
        page.getByPlaceholder("Enter email id (@planetngtech.com)").fill(userEmail);

        selectUserRole(userRole);
    }

    public void submitClientForm() {
        Locator saveButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Save"))
                .first();
        saveButton.scrollIntoViewIfNeeded();
        saveButton.click();

        Locator confirmModal = page.locator("div.fixed.inset-0")
                .filter(new Locator.FilterOptions().setHasText("save the client details"));
        confirmModal.waitFor();

        Response saveResponse = page.waitForResponse(
                response -> response.url().contains("AddOrEditClient"),
                new Page.WaitForResponseOptions().setTimeout(120000),
                () -> confirmModal.getByRole(
                        AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Save"))
                        .click(new Locator.ClickOptions().setForce(true)));

        String saveBody = saveResponse.text();
        if (!saveBody.contains("\"StatusCode\":1")) {
            throw new RuntimeException("Client save API failed: " + saveBody);
        }

        try {
            page.waitForResponse(
                    response -> response.url().contains("uploadClientFile"),
                    new Page.WaitForResponseOptions().setTimeout(60000),
                    () -> { });
        } catch (com.microsoft.playwright.PlaywrightException ignored) {
            // Upload may finish before the listener attaches.
        }

        Locator okButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("OK"));
        try {
            okButton.waitFor(new Locator.WaitForOptions().setTimeout(15000));
            okButton.click(new Locator.ClickOptions().setForce(true));
        } catch (com.microsoft.playwright.PlaywrightException ignored) {
            String clientsUrl = page.url().replace("/clients/add", "/clients");
            page.navigate(clientsUrl);
        }

        page.waitForURL(
                url -> isPostSaveUrl(url),
                new Page.WaitForURLOptions().setTimeout(60000));

        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public boolean isSaveSuccessful() {
        return isPostSaveUrl(page.url());
    }

    public boolean isClientListed(String searchText) {
        page.waitForLoadState(LoadState.NETWORKIDLE);

        Locator searchBox = page.getByPlaceholder("Search", new Page.GetByPlaceholderOptions().setExact(false));
        if (searchBox.count() > 0) {
            searchBox.first().fill(searchText);
            page.keyboard().press("Enter");
            page.waitForTimeout(3000);
        }

        return page.getByText(searchText, new Page.GetByTextOptions().setExact(false)).count() > 0;
    }

    private void attachImage(int uploadButtonIndex, Path imagePath) {
        Locator uploadButton = page.getByText("Upload", new Page.GetByTextOptions().setExact(true))
                .nth(uploadButtonIndex);

        FileChooser chooser = page.waitForFileChooser(uploadButton::click);
        chooser.setFiles(imagePath);
        page.locator("img[alt='Preview']").nth(uploadButtonIndex).waitFor();
        page.waitForTimeout(500);
    }

    private void selectUserRole(String role) {
        page.locator("div")
                .filter(new Locator.FilterOptions().setHasText("Section 4: System Users"))
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("- Select -"))
                .first()
                .click();
        page.getByText(role, new Page.GetByTextOptions().setExact(true))
                .first()
                .click();
        page.waitForTimeout(500);
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
        page.waitForTimeout(500);
    }

    public boolean isAddClientButtonVisible() {
        Locator addClientButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add Client"));
        if (addClientButton.count() > 0 && addClientButton.first().isVisible()) {
            return true;
        }
        return page.locator("button.btn-add, button:has-text('Add Client')").first().isVisible();
    }

    public void searchClients(String searchText) {
        Locator searchBox = page.getByPlaceholder("Search", new Page.GetByPlaceholderOptions().setExact(false));
        if (searchBox.count() > 0) {
            searchBox.first().fill(searchText);
            page.keyboard().press("Enter");
            page.waitForTimeout(2000);
        }
    }

    public boolean isBasicDetailsVisible() {
        return page.locator("body").innerText().toLowerCase().contains("basic details");
    }

    public boolean isClientCodeVisible() {
        String body = page.locator("body").innerText().toLowerCase();
        return body.contains("client code")
                || page.locator("input[disabled], input[readonly]").count() > 0;
    }

    public boolean isClientNamePlaceholderVisible() {
        return page.getByPlaceholder("Enter client name").isVisible();
    }

    public void fillClientName(String name) {
        page.getByPlaceholder("Enter client name").fill(name);
    }

    public boolean isDescriptionPlaceholderVisible() {
        return page.getByPlaceholder("Enter description").isVisible();
    }

    public boolean isOfficeAddressPlaceholderVisible() {
        return page.getByPlaceholder("Enter office address").isVisible();
    }

    public boolean isCountryDropdownVisible() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("- Select -")).count() > 0;
    }

    public boolean isPocNamePlaceholderVisible() {
        return page.getByPlaceholder("Enter POC name").isVisible();
    }

    public boolean isPocEmailPlaceholderVisible() {
        return page.getByPlaceholder("Enter POC email").isVisible();
    }

    public void fillPocEmail(String email) {
        page.getByPlaceholder("Enter POC email").fill(email);
    }

    public boolean isPocMobilePlaceholderVisible() {
        return page.getByPlaceholder("Enter POC mobile number").isVisible();
    }

    public boolean isBrandingSectionVisible() {
        String body = page.locator("body").innerText().toLowerCase();
        return body.contains("branding") && page.getByPlaceholder("globaltechsolutions").isVisible();
    }

    public boolean isLicenseSectionVisible() {
        return page.getByPlaceholder("Enter number of licenses").isVisible();
    }

    public boolean isPaginationVisible() {
        String body = page.locator("body").innerText().toLowerCase();
        return body.contains("previous") || body.contains("next");
    }

    public boolean hasNoRecordsMessage() {
        String body = page.locator("body").innerText().toLowerCase();
        return body.contains("no records") || body.contains("no data");
    }

    public void fillBrandingUrl(String brandingUrl) {
        page.getByPlaceholder("globaltechsolutions").fill(brandingUrl);
        page.keyboard().press("Tab");
        page.waitForTimeout(500);
    }

    public String getBrandingUrlValue() {
        return page.getByPlaceholder("globaltechsolutions").inputValue();
    }

    public void clickSaveWithoutConfirm() {
        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Save"))
                .first()
                .scrollIntoViewIfNeeded();
        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Save"))
                .first()
                .click();
        page.waitForTimeout(1000);
    }

    public boolean hasBrandingMandatoryError() {
        String bodyText = page.locator("body").innerText().toLowerCase();
        return bodyText.contains("branding") && bodyText.contains("mandatory");
    }

    public boolean isBrandingFieldInvalid() {
        Locator brandingField = page.getByPlaceholder("globaltechsolutions");
        String ariaInvalid = brandingField.getAttribute("aria-invalid");
        if ("true".equalsIgnoreCase(ariaInvalid)) {
            return true;
        }
        return hasBrandingMandatoryError();
    }

    public boolean hasValidationError() {
        String bodyText = page.locator("body").innerText().toLowerCase();
        return bodyText.matches("(?s).*(required|invalid|must|cannot|please enter|error|mandatory).*");
    }

    public boolean isStillOnAddClientForm() {
        return page.url().contains("/clients/add")
                && page.getByPlaceholder("Enter client name").isVisible();
    }
}

