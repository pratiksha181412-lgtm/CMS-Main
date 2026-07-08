package tests;

import base.AuthenticatedBaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.ClientPage;
import pages.DashboardPage;
import pages.RolesPage;
import utils.ConfigReader;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LoginTest extends AuthenticatedBaseTest {

    private static final String BASE_URL = ConfigReader.getProperty("url");

    @Test(groups = "e2e")
    public void verifyLoginOpenFormAndSaveClient() {
        ClientPage clientPage = new ClientPage(page);
        clientPage.openAddClientForm(BASE_URL);

        Assert.assertTrue(clientPage.isAddClientFormVisible(), "Add Client form was not displayed.");

        String uniqueId = randomAlpha(10);
        String clientName = "Auto Test Client " + uniqueId;
        String userEmail = "autouser" + uniqueId + "@planetngtech.com";
        String brandingUrl = "auto" + randomAlpha(8);

        clientPage.fillMandatoryClientForm(
                clientName,
                ConfigReader.getProperty("client.description"),
                ConfigReader.getProperty("client.officeAddress"),
                ConfigReader.getProperty("client.country"),
                ConfigReader.getProperty("client.timezone"),
                ConfigReader.getProperty("client.pocName"),
                ConfigReader.getProperty("client.pocEmail"),
                ConfigReader.getProperty("client.pocMobile"),
                brandingUrl,
                ConfigReader.getProperty("client.licenses"),
                ConfigReader.getProperty("client.startDate"),
                ConfigReader.getProperty("client.endDate"),
                testImagePath(),
                testImagePath(),
                ConfigReader.getProperty("client.firstName"),
                ConfigReader.getProperty("client.lastName"),
                userEmail,
                ConfigReader.getProperty("client.userRole"));

        clientPage.submitClientForm();

        DashboardPage dashboardPage = new DashboardPage(page);
        dashboardPage.waitForDashboard();
        Assert.assertTrue(
                dashboardPage.isDashboardVisible(),
                "Expected dashboard (clients list) after save but was: " + page.url());

        dashboardPage.openRoles();

        RolesPage rolesPage = new RolesPage(page);
        rolesPage.waitForRolesPage();
        Assert.assertTrue(
                rolesPage.isRolesPageVisible(),
                "Roles page did not open. Current URL: " + page.url());

        rolesPage.openAddRoleForm();
        Assert.assertTrue(
                rolesPage.isAddRoleFormVisible(),
                "Add Role form was not displayed. Current URL: " + page.url());

        rolesPage.fillAddRoleForm(
                ConfigReader.getProperty("role.name"),
                ConfigReader.getProperty("role.description"),
                ConfigReader.getProperty("role.type"));

        Assert.assertTrue(
                rolesPage.areAllSection2CheckboxesSelected(),
                "Not all Section 2 activity checkboxes were selected.");
    }

    private Path testImagePath() {
        for (String name : new String[] { "test-logo.jpg", "test-logo.png" }) {
            var resource = getClass().getClassLoader().getResource(name);
            if (resource != null) {
                try {
                    return Paths.get(resource.toURI());
                } catch (URISyntaxException e) {
                    throw new RuntimeException("Invalid test image path", e);
                }
            }
        }
        return Path.of("src/test/resources/test-logo.png");
    }

    private static String randomAlpha(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + (int) (Math.random() * 26)));
        }
        return sb.toString();
    }
}
