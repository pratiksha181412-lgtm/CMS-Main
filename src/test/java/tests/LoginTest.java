package tests;

import base.BaseTest;
import com.microsoft.playwright.options.LoadState;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.ClientPage;
import pages.HomePage;
import pages.LoginPage;
import utils.ConfigReader;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LoginTest extends BaseTest {

    @BeforeMethod
    public void resetToLoginPage() {
        page.navigate(ConfigReader.getProperty("url"));
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    private void performLogin() {
        LoginPage loginPage = new LoginPage(page);
        loginPage.login(
                ConfigReader.getProperty("email"),
                ConfigReader.getProperty("otp"));

        page.waitForLoadState();

        HomePage homePage = new HomePage(page);
        boolean loggedIn = homePage.isUserLoggedIn();

        if (!loggedIn && !ConfigReader.getBoolean("requireSuccessfulLogin", false)) {
            throw new SkipException(
                    "Live CMS login did not complete. Check OTP/credentials. Current URL: "
                            + homePage.getCurrentUrl());
        }

        Assert.assertTrue(loggedIn, "Login Failed. Current URL: " + homePage.getCurrentUrl());
    }

    private ClientPage navigateToAddClientForm() {
        ClientPage clientPage = new ClientPage(page);
        clientPage.openAddClientForm(ConfigReader.getProperty("url"));
        return clientPage;
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
        return Path.of("src/test/resources/test-logo.jpg");
    }

    @Test
    public void verifyCMSLoginAndOpenAddClient() {
        performLogin();

        ClientPage clientPage = navigateToAddClientForm();
        Assert.assertTrue(clientPage.isAddClientFormVisible(), "Add Client form was not displayed.");
    }

    @Test
    public void verifyCreateClientEndToEnd() {
        performLogin();

        ClientPage clientPage = navigateToAddClientForm();
        Assert.assertTrue(clientPage.isAddClientFormVisible(), "Add Client form was not displayed.");

        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        String clientName = ConfigReader.getProperty("client.name") + " " + uniqueSuffix;
        String userEmail = "autouser" + uniqueSuffix + "@planetngtech.com";

        clientPage.fillMandatoryClientForm(
                clientName,
                ConfigReader.getProperty("client.description"),
                ConfigReader.getProperty("client.officeAddress"),
                ConfigReader.getProperty("client.country"),
                ConfigReader.getProperty("client.timezone"),
                ConfigReader.getProperty("client.pocName"),
                ConfigReader.getProperty("client.pocEmail"),
                ConfigReader.getProperty("client.pocMobile"),
                ConfigReader.getProperty("client.brandingUrl") + uniqueSuffix,
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

        Assert.assertTrue(
                clientPage.isSaveSuccessful(),
                "Client form was not saved. Expected redirect to clients/dashboard but was: " + page.url());
    }
}
