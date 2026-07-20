package base;

import java.io.File;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import utils.ResetPolicy;

public class MastodonBase {

    // ---------- Constants ----------
    private static final int WAIT_TIMEOUT = 30;

    // ---------- Instance variables ----------
    protected WebDriverWait wait;

    // ---------- Thread Locals ----------
    private static final ThreadLocal<AppiumDriverLocalService> TL_SERVICE = new ThreadLocal<>();
    private static final ThreadLocal<AndroidDriver> TL_DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<Integer> TL_SERVER_PORT = new ThreadLocal<>();
    private static final ThreadLocal<Integer> TL_SYS_PORT = new ThreadLocal<>();
    private static final ThreadLocal<Integer> TL_CHROME_PORT = new ThreadLocal<>();

    protected AndroidDriver driver() { return TL_DRIVER.get(); }

    // ---------- port reservation ----------
    private static final Object PORT_LOCK = new Object();
    private static final Set<Integer> RESERVED = Collections.synchronizedSet(new HashSet<>());

    private static int pickFreePort() {
        for (int i = 0; i < 40; i++) {
            try (ServerSocket s = new ServerSocket(0)) {
                s.setReuseAddress(true);
                int p = s.getLocalPort();
                synchronized (PORT_LOCK) {
                    if (!RESERVED.contains(p)) {
                        RESERVED.add(p);
                        return p;
                    }
                }
            } catch (Exception ignored) {}
        }
        throw new RuntimeException("Could not find a free TCP port");
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket s = new ServerSocket(port)) {
            s.setReuseAddress(true);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void releasePort(Integer p) {
        if (p != null) RESERVED.remove(p);
    }

    // ---------- per-test parameters ----------
    private String udid;
    private int serverPort;
    private int systemPort;
    private int chromedriverPort;
    private boolean defaultNoReset;

    @Parameters({"udid","serverPort","systemPort","chromedriverPort","noReset"})
    @BeforeClass(alwaysRun = true)
    public void initParams(@Optional("emulator-5554")  String udid,
                           @Optional("4724") String serverPort,
                           @Optional("0") String systemPort,
                           @Optional("0") String chromePort,
                           @Optional("true") String noReset) {
        this.udid = udid;
        this.serverPort = Integer.parseInt(serverPort);
        this.systemPort = Integer.parseInt(systemPort);
        this.chromedriverPort = Integer.parseInt(chromePort);
        this.defaultNoReset = Boolean.parseBoolean(noReset);
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp(Method testMethod) throws Exception {
        // ---------- resolve ResetPolicy ----------
        ResetPolicy.Mode mode = ResetPolicy.Mode.INHERIT;
        ResetPolicy mAnno = testMethod.getAnnotation(ResetPolicy.class);
        if (mAnno != null) mode = mAnno.value();
        if (mode == ResetPolicy.Mode.INHERIT) {
            ResetPolicy cAnno = this.getClass().getAnnotation(ResetPolicy.class);
            if (cAnno != null) mode = cAnno.value();
        }
        if (mode == ResetPolicy.Mode.INHERIT) {
            mode = defaultNoReset ? ResetPolicy.Mode.NO_RESET : ResetPolicy.Mode.RESET_DATA;
        }

        // ----- auto-pick ports if XML passed 0 -----
        int srvPort    = (serverPort <= 0 || !isPortAvailable(serverPort)) ? pickFreePort() : serverPort;
        int sysPort    = (systemPort <= 0) ? pickFreePort() : systemPort;
        int chromePort = (chromedriverPort <= 0) ? pickFreePort() : chromedriverPort;

        TL_SERVER_PORT.set(srvPort);
        TL_SYS_PORT.set(sysPort);
        TL_CHROME_PORT.set(chromePort);

        System.out.printf("[SETUP] udid=%s  serverPort=%d  systemPort=%d  chromedriverPort=%d  reset=%s%n",
                udid, srvPort, sysPort, chromePort, mode);

        // ----- start local Appium server -----
        File node = new File("C:/Program Files/nodejs/node.exe");
        File appiumMainJs = new File("C:/Users/raxit/AppData/Roaming/npm/node_modules/appium/build/lib/main.js");
        // ----- start local Appium server -----
      
        

        AppiumDriverLocalService service = new AppiumServiceBuilder()
                .withAppiumJS(appiumMainJs)
                .usingDriverExecutable(node)
                .withIPAddress("127.0.0.1")
                .usingPort(srvPort)
                .withTimeout(Duration.ofSeconds(30))
                // Removed .withLogFile() entirely
                .build();
        
        // This magic line completely mutes the Appium server console output
        service.clearOutPutStreams(); 
        
        service.start();
        TL_SERVICE.set(service);

        // ----- Set Capabilities (Swag Labs App) -----
        // ----- Set Capabilities (Swag Labs App) -----
        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformName("Android")
                .setAutomationName("UiAutomator2")
                .setDeviceName(udid)
                .setAppPackage("com.swaglabsmobileapp")
                .setAppActivity(".SplashActivity")
                .setSystemPort(sysPort)
                .setNoReset(mode == ResetPolicy.Mode.NO_RESET);
                options.setCapability("appium:unicodeKeyboard", true);
                options.setCapability("appium:resetKeyboard", true);

        // ----- Initialize Driver -----
        AndroidDriver androidDriver = new AndroidDriver(service.getUrl(), options);
        TL_DRIVER.set(androidDriver);

        // Verify driver is properly initialized
        if (driver() == null) {
            Assert.fail("Driver is not initialized");
        }

        // Verify device connection
        try {
            System.out.println("Device Name: " + driver().getCapabilities().getCapability("deviceName"));
            System.out.println("Platform Version: " + driver().getCapabilities().getCapability("platformVersion"));
        } catch (Exception e) {
            System.out.println("Could not get device info: " + e.getMessage());
        }

        wait = new WebDriverWait(driver(), Duration.ofSeconds(WAIT_TIMEOUT));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        // Quit the driver securely
        if (driver() != null) {
            driver().quit();
            TL_DRIVER.remove();
        }

        try {
            AppiumDriverLocalService s = TL_SERVICE.get();
            if (s != null) s.stop();
        } catch (Exception ignored) {}
        finally {
            TL_SERVICE.remove();
        }

        releasePort(TL_SERVER_PORT.get());
        releasePort(TL_SYS_PORT.get());
        releasePort(TL_CHROME_PORT.get());
        TL_SERVER_PORT.remove();
        TL_SYS_PORT.remove();
        TL_CHROME_PORT.remove();
    }
}