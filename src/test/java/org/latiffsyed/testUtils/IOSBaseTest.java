package org.latiffsyed.testUtils;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;

import org.latiffsyed.core.utils.AppiumUtils;
import org.latiffsyed.pageobjects.iOS.HomePage;

import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.service.local.AppiumDriverLocalService;

public class IOSBaseTest extends AppiumUtils {

    public IOSDriver driver;
    public AppiumDriverLocalService service;
    public HomePage homePage;

    @BeforeClass(alwaysRun = true)
    public void configureAppium() throws IOException {
        // Load defaults from data.properties (used for local runs)
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(
                Paths.get(System.getProperty("user.dir"),
                          "src","main","java","org","latiffsyed","resources","data.properties").toString())) {
            prop.load(fis);
        }

        String ipAddress = System.getProperty("ipAddress", prop.getProperty("ipAddress", "127.0.0.1"));
        int port = Integer.parseInt(prop.getProperty("port", "4723"));

        System.out.println("[iOS] REMOTE_URL: " + getRemoteUrlProperty());

        // Start local Appium ONLY when REMOTE_URL is NOT provided (local dev)
        if (getRemoteUrlProperty().isEmpty()) {
            service = startAppiumServer(ipAddress, port);
        }

        // --- Capabilities ---
        XCUITestOptions options = new XCUITestOptions();

        // Device/simulator (system props override data.properties)
        String deviceName = System.getProperty("ios.device", prop.getProperty("iOSDeviceName", "iPhone 15"));
        options.setDeviceName(deviceName);

        String platformVersion = System.getProperty("ios.platformVersion", prop.getProperty("iOSPlatformVersion", ""));
        if (!platformVersion.isBlank()) {
            options.setPlatformVersion(platformVersion);  // optional in CI
        }

        // If CI passes a UDID, target the exact booted simulator
        String udid = System.getProperty("ios.udid", "").trim();
        if (!udid.isEmpty()) {
            options.setUdid(udid);
        }

        options.setAutomationName("XCUITest");
        options.setWdaLaunchTimeout(Duration.ofSeconds(120));

        // .app path (simulator build). System prop can override default path.
        String defaultAppPath = Paths.get(System.getProperty("user.dir"),
                "src","test","java","org","latiffsyed","resources","UIKitCatalog.app").toString();
        String appPath = System.getProperty("ios.app.path", defaultAppPath);
        System.out.println("[iOS] Using app: " + appPath);
        options.setApp(appPath);

        // Create driver pointing to CI server (REMOTE_URL) or local service
        driver = new IOSDriver(getServerUrlOrThrow(), options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        homePage = new HomePage(driver);
        System.out.println("[iOS] Driver/session started: " + deviceName +
                (udid.isEmpty() ? "" : " (" + udid + ")"));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        try {
            if (driver != null) driver.quit();
        } finally {
            // Only stop local service if we started it
            if (service != null && getRemoteUrlProperty().isEmpty()) {
                service.stop();
            }
        }
        System.out.println("[iOS] Teardown complete.");
    }
}