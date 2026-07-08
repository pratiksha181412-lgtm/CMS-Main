package base;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.testng.annotations.*;
import utils.ConfigReader;

public class BaseTest {

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    public Page getPage() {
        return page;
    }

    @BeforeClass
    public void setup() {
        playwright = Playwright.create();

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(ConfigReader.getBoolean("headless", false))
                .setSlowMo(ConfigReader.getInt("slowMo", 0));

        String browserName = ConfigReader.getProperty("browser", "chromium");
        BrowserType browserType = switch (browserName.toLowerCase()) {
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            default -> playwright.chromium();
        };

        browser = browserType.launch(launchOptions);
        context = browser.newContext();
        page = context.newPage();
        page.setDefaultTimeout(ConfigReader.getInt("timeout", 15000));
        page.setViewportSize(1536, 864);
        openApplication();
    }

    private void openApplication() {
        String appUrl = ConfigReader.getProperty("url");
        page.navigate(appUrl);
        page.waitForLoadState(LoadState.NETWORKIDLE);

        if (isAccessDeniedPage()) {
            throw new IllegalStateException(
                    "Sandbox returned 'Access Denied' instead of the login page at "
                            + page.url()
                            + ". The site may be down or your network cannot reach it. "
                            + "Try opening " + appUrl + " manually in Chrome, or contact your team to restore sandbox access.");
        }

        page.locator("input[type='email']")
                .waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(60000));
    }

    private boolean isAccessDeniedPage() {
        String content = page.content().toLowerCase();
        return content.contains("<code>accessdenied</code>")
                || content.contains("<message>access denied</message>")
                || (content.contains("access denied") && !page.locator("input[type='email']").isVisible());
    }

    protected void resetToLoginPage() {
        page.navigate(ConfigReader.getProperty("url"));
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.locator("input[type='email']")
                .waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(60000));
    }

    @AfterClass
    public void tearDown() {
        try {
            if (context != null) {
                context.close();
            }
            if (browser != null) {
                browser.close();
            }
            if (playwright != null) {
                playwright.close();
            }
        } catch (Exception ignored) {
            // Ignore Playwright driver cleanup errors on Windows temp folders.
        }
    }
}
