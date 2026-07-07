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
        page.navigate(ConfigReader.getProperty("url"));
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    @AfterClass
    public void tearDown() {
        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
