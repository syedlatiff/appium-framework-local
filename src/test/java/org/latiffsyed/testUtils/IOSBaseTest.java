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
        // Load properties (optional for local dev)
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(
                Paths.get(System.getProperty("user.dir"),
                          "src","main","java","org","latiffsyed","resources","data.properties").toString())) {
            prop.load(fis);
        }

        String ipAddress = System.getProperty("ipAddress", prop.getProperty("ipAddress", "127.0.0.1"));
        int port = Integer.parseInt(prop.getProperty("port", "4723"));

        System.out.println("[iOS] REMOTE_URL: " + getRemoteUrlProperty());

        // Start local Appium ONLY when REMOTE_URL is NOT provided (CI provides the server)
        if (getRemoteUrlProperty().isEmpty()) {
            service = startAppiumServer(ipAddress, port); // no-op in CI
        }

        // --- Capabilities ---
        XCUITestOptions options = new XCUITestOptions();

        // Device/simulator
        options.setDeviceName(System.getProperty("ios.device", prop.getProperty("iOSDeviceName", "iPhone 15")));
        String platformVersion = System.getProperty("ios.platformVersion", prop.getProperty("iOSPlatformVersion", ""));
        if (!platformVersion.isBlank()) {
            options.setPlatformVersion(platformVersion);
        }
        options.setAutomationName("XCUITest");
        options.setWdaLaunchTimeout(Duration.ofSeconds(60));

        // .app path (Simulator build). Allow override via -Dios.app.path
        String defaultAppPath = Paths.get(System.getProperty("user.dir"),
                "src","test","java","org","latiffsyed","resources","UIKitCatalog.app").toString();
        String appPath = System.getProperty("ios.app.path", defaultAppPath);
        System.out.println("[iOS] Using app: " + appPath);
        options.setApp(appPath);

        // Create driver pointing to REMOTE_URL (CI) or local service URL (dev)
        driver = new IOSDriver(getServerUrlOrThrow(), options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        homePage = new HomePage(driver);
        System.out.println("[iOS] Driver/session started.");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        try {
            if (driver != null) driver.quit();
        } finally {
            // Only stop local service if we started it (i.e., when REMOTE_URL not provided)
            if (service != null && getRemoteUrlProperty().isEmpty()) {
                service.stop();
            }
        }
        System.out.println("[iOS] Teardown complete.");
    }
}