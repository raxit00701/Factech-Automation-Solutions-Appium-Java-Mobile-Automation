Here is the stripped-down version of your framework foundation. It gives the reviewer exactly what they need to run the code (a working driver and basic Appium capabilities) while keeping your dynamic port routing, thread-safety logic, and automatic server builders safely off the internet.

Since your test classes import and use `@ResetPolicy(NO_RESET)`, I have also included a simple dummy annotation so you don't have to edit your test files to make them compile.

### 1. The Dummy Reset Policy (`src/main/java/utils/ResetPolicy.java`)

Put this in your `utils` package so your tests compile without modifying their imports.

```java
package utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ResetPolicy {
    Mode value() default Mode.INHERIT;

    enum Mode {
        NO_RESET,
        RESET_DATA,
        FAST_RESET,
        FULL_RESET,
        INHERIT
    }
}

```

### 2. The Bare-Bones Base Class (`src/main/java/base/MastodonBase.java`)

This simply sets the capabilities for the Sauce Labs app and connects to the Appium server that the reviewer will start manually via their terminal.

```java
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

```

---

### 3. The Updated README.md

I have removed the mentions of custom annotations and complex base class features so the reviewer's expectations align perfectly with the simplified code above.

```markdown
# 📱 Mobile Test Automation - Sauce Labs Demo App

This repository contains a mobile test automation framework built to automate user journeys on the Sauce Labs Demo Android App. 

This project was completed as part of the QA / Mobile Test Automation Intern assignment.

## 🛠 Tech Stack
* **Language:** Java (JDK 17)
* **Automation Tool:** Appium (v2.x)
* **Test Framework:** TestNG
* **Build Tool:** Maven
* **Reporting:** Allure Reports
* **Design Pattern:** Page Object Model (POM)

---

## 🏗 Framework Architecture

The framework is designed for readability and maintainability, utilizing the following architecture:

* **Page Object Model (POM):** UI locators and interactions are isolated in dedicated page classes (`LoginPage.java`, `Checkoutflow.java`) using Appium's `@AndroidFindBy` annotations and `AppiumFieldDecorator`. This separates test logic from UI logic.
* **Data-Driven Testing (DDT):** The login scenarios are driven by a custom CSV utility (`CsvUtils.java`) that feeds data into the TestNG `@DataProvider`. This allows the same test (`Test1.java`) to execute multiple positive and negative login flows without code duplication.
* **Base Class:** `MastodonBase.java` handles basic driver initialization, teardown, and mapping the required `UiAutomator2Options` for the target application. 
* **Explicit Waits:** The `WebDriverWait` class is heavily used within the Page Objects to handle dynamic rendering and ensure stable interactions.
* **Locators:** A mix of reliable locator strategies is used, favoring `accessibilityId` and `UiAutomator` for native scrolling capabilities.

## 🧪 Scenarios Automated

1. **Data-Driven Login (Positive & Negative):** Validates successful login and various failure states (locked out user, empty fields, invalid credentials) using data from `src/test/resources/data/Login.csv`.
2. **E2E Checkout Flow:** Logs in, navigates the catalog, adds a backpack to the cart, validates cart badge updates, fills out shipping details, and verifies the final order confirmation messages.

---

## ⚙️ Prerequisites & Setup

Ensure the following tools are installed on your machine:
1. **Java JDK 17+**: Ensure `JAVA_HOME` is set.
2. **Maven**: Ensure `mvn` is added to your system path.
3. **Node.js**: Required to run Appium.
4. **Appium 2.x**: Installed globally via npm (`npm i -g appium`).
5. **Appium UiAutomator2 Driver**: Installed via Appium (`appium driver install uiautomator2`).
6. **Android Studio / SDK**: Ensure `ANDROID_HOME` is set and you have a configured Android Emulator.
7. **Target Application:** Download the latest Sauce Labs Demo `.apk` from GitHub and ensure it is installed on your emulator.

---

## 🚀 Terminal Run Commands

1. **Start your Android Emulator** (Ensure the device UDID matches `emulator-5554` or update the Base Class / XML to match your device).
2. **Start the Appium Server:**
   ```bash
   appium --port 4724

```

3. **Execute the Test Suite via Maven:**
Open a new terminal window at the project root and run:
```bash
mvn clean test -DsuiteXmlFile=testng.xml

```



### Viewing the Test Report

This project integrates with Allure for detailed test execution reporting. After the test suite completes, generate and serve the report using:

```bash
allure serve allure-results

```

---

## 📊 Execution Logs

The framework logs detailed execution steps directly to the console during runtime to track interactions.

* **Expected TestNG Output:**
```text
==================================================
Running Test - Username: [standard_user] | Expected: [Success]
Clicking username field...
Entering username: standard_user
Clicking password field...
Entering password: secret_sauce
Clicking LOGIN button...
Waiting for login result...
RESULT: Login successful - 'PRODUCTS' displayed (As Expected).
==================================================

```



```

```
