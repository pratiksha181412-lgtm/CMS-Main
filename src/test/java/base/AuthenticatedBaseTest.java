package base;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import pages.HomePage;
import pages.LoginPage;
import utils.ConfigReader;

public class AuthenticatedBaseTest extends BaseTest {

    @BeforeClass(alwaysRun = true, dependsOnMethods = "setup")
    public void loginOnce() {
        LoginPage loginPage = new LoginPage(page);
        loginPage.login(
                ConfigReader.getProperty("email"),
                ConfigReader.getProperty("otp"));

        HomePage homePage = new HomePage(page);
        Assert.assertTrue(
                homePage.isUserLoggedIn(),
                "Login failed. Current URL: " + homePage.getCurrentUrl());
    }
}
