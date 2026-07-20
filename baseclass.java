package base;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class MastodonBase {

    private AndroidDriver driver;

    // Provide driver to Page Objects and Tests
    protected AndroidDriver driver() {
        return driver;
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws MalformedURLException {
        System.out.println("[SETUP] Initializing AndroidDriver for Sauce Labs App...");

        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformName("Android")
                .setAutomationName("UiAutomator2")
                .setDeviceName("emulator-5554")
                .setAppPackage("com.swaglabsmobileapp")
                .setAppActivity(".SplashActivity")
                .setNoReset(true); // Hardcoded for the assignment to avoid complexity

        // Connect to the manually started Appium server on port 4724
        driver = new AndroidDriver(new URL("http://127.0.0.1:4724"), options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            System.out.println("[TEARDOWN] Quitting Driver...");
            driver.quit();
        }
    }
}
