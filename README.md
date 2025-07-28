# Appium Framework - Local Execution (Android & iOS)

This is a local Appium automation framework for executing Android and iOS test cases on emulators or real devices. It uses Java, TestNG, and Maven, following a modular Page Object Model (POM) structure for reusability and maintainability.

---

## 🚀 Features

- 🔹 Supports both Android and iOS platforms
- 🔹 Designed for **local execution** on emulator or real device
- 🔹 Page Object Model (POM) structure
- 🔹 TestNG-based test orchestration
- 🔹 ExtentReports HTML reporting
- 🔹 JSON and `.properties` for test data management

---

## 🧱 Project Structure

```
AppiumFrameworkDesign/
├── src/main/java
│   └── org.latiffsyed.core.utils           # Utility functions
│   └── org.latiffsyed.pageobjects.android  # Android screen objects
│   └── org.latiffsyed.pageobjects.iOS      # iOS screen objects
│   └── org.latiffsyed.resources            # Data handling utilities
│   └── org.latiffsyed.testData             # Test data (JSON, properties)
├── src/test/java
│   └── org.latiffsyed.tests.android        # Android test cases
│   └── org.latiffsyed.tests.iOS            # iOS test cases
│   └── org.latiffsyed.testUtils            # Base classes, Listeners, Reporters
│   └── org.latiffsyed.resources            # Test app files (.apk, .app)
├── testNGSuite/                            # TestNG XML files
├── test-output/                            # TestNG execution results
├── reports/                                # ExtentReports HTML output
├── pom.xml                                 # Maven configuration
```

---

## 🧪 How to Run Tests Locally

### 1. 📦 Install dependencies
```bash
mvn clean install
```

### 2. ⚙️ Start Appium Server
You can use Appium Desktop or run from CLI:
```bash
appium
```

### 3. 📱 Connect Device or Start Emulator
Ensure Android/iOS device is connected or emulator is running.

### 4. 🚀 Run Tests
```bash
mvn test -DsuiteXmlFile=testng_Smoke.xml
```

Or
```bash
mvn test -DsuiteXmlFile=testng.xml
```

---

## 🖼️ Reports

View test reports at:
```
reports/index.html
```
Also includes screenshot examples like:
- `reportsFormLogin_ErrorValidationFailedTest.png`

---

## 🧰 Tech Stack

- Java 17
- Appium
- TestNG
- Maven
- ExtentReports
- Local devices/emulators
- JSON & Properties for test data

---

## ✍️ Author

**Latiff Syed**  
📧 [YourEmail@example.com]  
🔗 [https://github.com/syedlatiff](https://github.com/syedlatiff)

---

## 📄 License

This project is licensed under the MIT License.