package org.latiffsyed.testUtils;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;

import org.latiffsyed.core.utils.AppiumUtils;
import org.latiffsyed.pageobjects.android.FormPage;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.service.local.AppiumDriverLocalService;

public class AndroidBaseTest extends AppiumUtils {

    public AndroidDriver driver;
    public AppiumDriverLocalService service;
    public FormPage formPage;

    private boolean isRunningOnCI() {
        return System.getenv("GITHUB_ACTIONS") != null || System.getenv("CI") != null;
    }

    private boolean isRunningOnMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    @BeforeClass(alwaysRun = true)
    public void configureAppium() throws IOException {
        // --- Load properties ---
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(
                Paths.get(System.getProperty("user.dir"),
                          "src", "main", "java", "org", "latiffsyed", "resources", "data.properties").toString())) {
            prop.load(fis);
        }

        String ipAddress = System.getProperty("ipAddress", prop.getProperty("ipAddress"));
        int port = Integer.parseInt(prop.getProperty("port"));

        System.out.println("Environment:");
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("CI: " + isRunningOnCI());
        System.out.println("REMOTE_URL: " + getRemoteUrlProperty());
        System.out.println("IP: " + ipAddress + "  Port: " + port);

        // --- Start local Appium only when REMOTE_URL is NOT provided (i.e., local dev) ---
        if (getRemoteUrlProperty().isEmpty()) {
            service = startAppiumServer(ipAddress, port); // from AppiumUtils (no-op in CI)
        }

        // --- Capabilities ---
        UiAutomator2Options options = new UiAutomator2Options();

        // Device
        if (isRunningOnCI()) {
            options.setDeviceName(System.getProperty("androidDevice", "emulator-5554"));
        } else {
            options.setDeviceName(prop.getProperty("AndroidDeviceName"));
        }

        // Chromedriver (local macOS only; CI uses Appium auto-download)
        if (isRunningOnMac() && !isRunningOnCI()) {
            String chromeDriverPath =
                    Paths.get("/Users", "administrator", "Documents",
                              "Chromedriver 138.0.7204.92", "chromedriver-mac-x64", "chromedriver").toString();
            System.out.println("Setting chromedriver path (local macOS): " + chromeDriverPath);
            options.setChromedriverExecutable(chromeDriverPath);
        } else {
            System.out.println("Chromedriver: letting Appium handle automatically");
        }

        // App under test
        String appPath = Paths.get(System.getProperty("user.dir"),
                                   "src", "test", "java", "org", "latiffsyed", "resources",
                                   "General-Store.apk").toString();
        System.out.println("App path: " + appPath);
        options.setApp(appPath);

        // Stability tweaks
        options.setCapability("appium:automationName", "UiAutomator2");
        options.setCapability("appium:autoGrantPermissions", true);
        options.setCapability("appium:ignoreHiddenApiPolicyError", true);
        options.setCapability("appium:disableWindowAnimation", true);

        // --- Create driver pointing to either REMOTE_URL (CI) or local service URL (dev) ---
        driver = new AndroidDriver(getServerUrlOrThrow(), options); // from AppiumUtils
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // Page object
        formPage = new FormPage(driver);

        System.out.println("Appium configuration completed successfully.");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        try {
            if (driver != null) driver.quit();
        } finally {
            // Only stop local service if we started it (i.e., no REMOTE_URL)
            if (service != null && getRemoteUrlProperty().isEmpty()) {
                service.stop();
            }
        }
        System.out.println("Appium teardown completed.");
    }
}