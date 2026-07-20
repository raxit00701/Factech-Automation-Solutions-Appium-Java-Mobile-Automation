
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
