package org.latiffsyed.testUtils;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import java.io.FileInputStream;
import java.io.IOException;
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
    
    // Helper method to detect if running on CI (GitHub Actions)
    private boolean isRunningOnCI() {
        return System.getenv("GITHUB_ACTIONS") != null || 
               System.getenv("CI") != null;
    }
    
    // Helper method to detect macOS
    private boolean isRunningOnMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }
    
    @BeforeClass(alwaysRun=true)
    public void configureAppium() throws IOException {
        Properties prop = new Properties();
        FileInputStream fis = new FileInputStream(
            System.getProperty("user.dir") + "//src//main//java//org//latiffsyed//resources//data.properties"
        );
        prop.load(fis);
        
        String ipAddress = System.getProperty("ipAddress") != null ? 
                          System.getProperty("ipAddress") : prop.getProperty("ipAddress");
        String port = prop.getProperty("port");
        
        System.out.println("Environment Detection:");
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Running on CI: " + isRunningOnCI());
        System.out.println("Running on Mac: " + isRunningOnMac());
        System.out.println("IP: " + ipAddress);
        System.out.println("Port: " + port);
        
        service = startAppiumServer(ipAddress, Integer.parseInt(port));
        
        UiAutomator2Options options = new UiAutomator2Options();
        
        // Device name configuration
        if (isRunningOnCI()) {
            // GitHub Actions - use emulator
            options.setDeviceName(System.getProperty("androidDevice", "emulator-5554"));
        } else {
            // Local development - use property or default
            options.setDeviceName(prop.getProperty("AndroidDeviceName"));
        }
        
        // Chromedriver configuration - ONLY for macOS local development
        if (isRunningOnMac() && !isRunningOnCI()) {
            String chromeDriverPath = "//Users//administrator//Documents//Chromedriver 138.0.7204.92//chromedriver-mac-x64//chromedriver";
            System.out.println("Setting chromedriver path for macOS: " + chromeDriverPath);
            options.setChromedriverExecutable(chromeDriverPath);
        } else {
            System.out.println("Letting Appium handle chromedriver automatically");
            // Appium will auto-download and manage chromedriver for CI
        }
        
        // App path
        String appPath = System.getProperty("user.dir") + "//src//test//java//org//latiffsyed//resources//General-Store.apk";
        System.out.println("App path: " + appPath);
        options.setApp(appPath);
        
        // Additional capabilities for better stability
        options.setCapability("appium:automationName", "UiAutomator2");
        options.setCapability("appium:autoGrantPermissions", true);
        options.setCapability("appium:ignoreHiddenApiPolicyError", true);
        options.setCapability("appium:disableWindowAnimation", true);
        
        driver = new AndroidDriver(service.getUrl(), options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        formPage = new FormPage(driver);
        
        System.out.println("Appium configuration completed successfully");
    }
    
    @AfterClass(alwaysRun=true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        if (service != null) {
            service.stop();
        }
        System.out.println("Appium teardown completed");
    }
}