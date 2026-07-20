package base;

import io.appium.java_client.Setting;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import utils.ResetPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class DriverFactory {

    private static final ThreadLocal<AndroidDriver> TL_DRIVER = new ThreadLocal<>();

    public static AndroidDriver getDriver() {
        return TL_DRIVER.get();
    }

    public static void quitDriver() {
        AndroidDriver d = TL_DRIVER.get();
        if (d != null) {
            try { d.quit(); } catch (Exception ignored) {}
            TL_DRIVER.remove();
        }
    }

    public static void initDriver(
            String udid,
            int serverPort,
            int systemPort,
            int chromePort,
            ResetPolicy.Mode resetMode
    ) throws MalformedURLException {

        boolean noResetLocal, fullResetLocal;
        switch (resetMode) {
            case NO_RESET:
                noResetLocal = true;  fullResetLocal = false;
                break;
            case RESET_DATA:
                noResetLocal = false; fullResetLocal = false; // app data cleared
                break;
            case FAST_RESET:
                noResetLocal = false; fullResetLocal = false;
                // Add special capability for "fastReset"
                break;
            case FULL_RESET:
                noResetLocal = false; fullResetLocal = true;
                break;
            case INHERIT:
            default:
                noResetLocal = Boolean.parseBoolean(System.getProperty("noReset", "true"));
                fullResetLocal = false;
                break;
        }

        UiAutomator2Options options = new UiAutomator2Options();

        // Device / transport
        options.setDeviceName((udid == null || udid.isBlank()) ? "Android" : udid);
        if (udid != null && !udid.isBlank()) options.setUdid(udid);

        // ---- App under test (allow overrides via -D system props to avoid hard edits here)
        String appPackage  = System.getProperty("appPackage",  "com.myhotels.sa");
        String appActivity = System.getProperty("appActivity", "com.genxesolution.base.activities.ActivitySplash");
        options.setAppPackage(appPackage);
        options.setAppActivity(appActivity);
        options.setAppWaitActivity("*");

        // ---- Timeouts & QoL
        options.setAdbExecTimeout(Duration.ofSeconds(60));
        options.setNewCommandTimeout(Duration.ofSeconds(300));
        options.setCapability("appWaitForQuiescence", false);
        options.setCapability("disableWindowAnimation", true);
        options.setCapability("autoGrantPermissions", true);
        options.setCapability("unicodeKeyboard", true);
        options.setCapability("resetKeyboard", true);

        // ---- Parallel-friendly ports
        options.setCapability("systemPort", systemPort);
        options.setCapability("chromedriverPort", chromePort);

        // ---- Reset modes
        options.setCapability("noReset", noResetLocal);
        options.setCapability("fullReset", fullResetLocal);
        if (resetMode == ResetPolicy.Mode.FAST_RESET) {
            options.setCapability("fastReset", true);
        }

        // ---- ChromeDriver (allow override via -DchromedriverExecutable)
        String chromeExec = System.getProperty("chromedriverExecutable", "C:\\chromedriver.exe");
        options.setCapability("chromedriverExecutable", chromeExec);

        // ---- Start driver
        String serverUrl = "http://127.0.0.1:" + serverPort;
        AndroidDriver d;
        try {
            d = new AndroidDriver(new URL(serverUrl), options);
        } catch (org.openqa.selenium.SessionNotCreatedException e) {
            String msg = String.valueOf(e.getMessage());
            if (msg.contains("local port") && msg.contains("busy")) {
                // Retry ONCE with bumped ports to escape race
                int newSystemPort = systemPort + 1;
                int newChromePort = chromePort + 2;
                options.setCapability("systemPort", newSystemPort);
                options.setCapability("chromedriverPort", newChromePort);
                System.out.printf("[DRIVER-FACTORY][RETRY] systemPort %d busy → retry systemPort=%d chromePort=%d%n",
                        systemPort, newSystemPort, newChromePort);
                d = new AndroidDriver(new URL(serverUrl), options);
            } else {
                throw e;
            }
        }
        TL_DRIVER.set(d);


        // Make element-ready faster
        try { d.setSetting(Setting.WAIT_FOR_IDLE_TIMEOUT, 0); } catch (Exception ignored) {}

        System.out.printf(
                "[DRIVER-FACTORY] udid=%s server=%d system=%d chrome=%d reset=%s (noReset=%s fullReset=%s) app=%s/%s%n",
                udid, serverPort, systemPort, chromePort, resetMode, noResetLocal, fullResetLocal, appPackage, appActivity
        );
    }
}
