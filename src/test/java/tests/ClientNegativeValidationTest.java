package tests;

import base.AuthenticatedBaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.ClientPage;
import utils.ConfigReader;
import utils.ExcelTestCaseReader;
import utils.TestCase;

public class ClientNegativeValidationTest extends AuthenticatedBaseTest {

    private static final String BASE_URL = ConfigReader.getProperty("url");

    @Test(groups = "negative")
    public void tc24_brandingEmptyNotAllowed() {
        TestCase testCase = ExcelTestCaseReader.findByTcId("Negative", "TC24")
                .orElseThrow(() -> new AssertionError("TC24 not found in Excel"));

        ClientPage clientPage = new ClientPage(page);
        clientPage.openAddClientForm(BASE_URL);
        clientPage.fillBrandingUrl("");
        clientPage.clickSaveWithoutConfirm();

        Assert.assertTrue(
                clientPage.isBrandingFieldInvalid()
                        || clientPage.hasBrandingMandatoryError()
                        || (clientPage.hasValidationError() && clientPage.isStillOnAddClientForm()),
                testCase.getExpectedResults());
    }

    @Test(groups = "negative")
    public void tc25_brandingSpecialCharactersRejected() {
        TestCase testCase = ExcelTestCaseReader.findByTcId("Negative", "TC25")
                .orElseThrow(() -> new AssertionError("TC25 not found in Excel"));

        ClientPage clientPage = new ClientPage(page);
        clientPage.openAddClientForm(BASE_URL);
        clientPage.fillBrandingUrl("auto@test#");
        clientPage.clickSaveWithoutConfirm();

        String brandingValue = clientPage.getBrandingUrlValue();
        Assert.assertFalse(brandingValue.contains("@") || brandingValue.contains("#"),
                testCase.getExpectedResults());
    }

    @Test(groups = "negative")
    public void tc26_brandingSpacesRejected() {
        TestCase testCase = ExcelTestCaseReader.findByTcId("Negative", "TC26")
                .orElseThrow(() -> new AssertionError("TC26 not found in Excel"));

        ClientPage clientPage = new ClientPage(page);
        clientPage.openAddClientForm(BASE_URL);
        clientPage.fillBrandingUrl("auto test");
        clientPage.clickSaveWithoutConfirm();

        Assert.assertFalse(
                clientPage.getBrandingUrlValue().contains(" "),
                testCase.getExpectedResults());
    }

    @Test(groups = "negative")
    public void tc27_brandingMaxLengthEnforced() {
        TestCase testCase = ExcelTestCaseReader.findByTcId("Negative", "TC27")
                .orElseThrow(() -> new AssertionError("TC27 not found in Excel"));

        ClientPage clientPage = new ClientPage(page);
        clientPage.openAddClientForm(BASE_URL);
        clientPage.fillBrandingUrl("abcdefghijklmnopqrstuvwxyz");

        Assert.assertTrue(
                clientPage.getBrandingUrlValue().length() <= 20,
                testCase.getExpectedResults());
    }
}
