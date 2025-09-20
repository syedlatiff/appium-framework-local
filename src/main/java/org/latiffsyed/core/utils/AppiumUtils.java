package org.latiffsyed.core.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;

/**
 * CI-safe Appium utilities.
 *
 * • In CI: pass -DREMOTE_URL=http://127.0.0.1:4723/ and we will NOT try to start a local Appium.
 * • Local dev: if REMOTE_URL is absent, we attempt to start a local Appium and try several common
 *   locations for appium/build/lib/main.js (override with env APPIUM_MAIN if needed).
 */
public class AppiumUtils {

    public AppiumDriverLocalService service;

    /** Read the optional remote URL property used in CI. */
    public static String getRemoteUrlProperty() {
        return System.getProperty("REMOTE_URL", "").trim();
    }

    /** Start a local Appium server (skipped automatically when REMOTE_URL is provided). */
    public AppiumDriverLocalService startAppiumServer(String ipAddress, int port) {
        // CI path: do not start a local server here; the workflow already starts it.
        String remoteUrl = getRemoteUrlProperty();
        if (!remoteUrl.isEmpty()) {
            return null; // signal that CI is providing the server
        }

        File mainJs = resolveAppiumMainJs();

        service = new AppiumServiceBuilder()
                .withAppiumJS(mainJs)
                .withIPAddress(ipAddress)
                .usingPort(port)
                .build();
        service.start();
        return service;
    }

    /** Resolve appium/build/lib/main.js robustly (supports local dev on different machines). */
    private static File resolveAppiumMainJs() {
        // Highest priority: explicit override (useful on dev machines)
        String override = System.getenv("APPIUM_MAIN");
        if (override != null && !override.isBlank()) {
            File f = new File(override);
            if (f.exists()) return f;
        }

        // Common install paths across macOS/Linux and GitHub runners
        String[] candidates = new String[] {
            "/usr/local/lib/node_modules/appium/build/lib/main.js",
            "/opt/homebrew/lib/node_modules/appium/build/lib/main.js",
            "/usr/lib/node_modules/appium/build/lib/main.js",
            // Example path pattern sometimes seen on GitHub runners:
            "/opt/hostedtoolcache/node/20.*/x64/lib/node_modules/appium/build/lib/main.js"
        };

        for (String path : candidates) {
            // Support the wildcard entry above in a simple way
            if (path.contains("*")) {
                File hosted = new File("/opt/hostedtoolcache/node");
                File[] versions = hosted.listFiles();
                if (versions != null) {
                    for (File v : versions) {
                        File f = new File(v, "x64/lib/node_modules/appium/build/lib/main.js");
                        if (f.exists()) return f;
                    }
                }
            } else {
                File f = new File(path);
                if (f.exists()) return f;
            }
        }

        // Fallback to the original default (may work on some dev boxes)
        return new File("/usr/local/lib/node_modules/appium/build/lib/main.js");
    }

    /** Get the server URL to use for driver creation (CI remote or local service). */
    public URL getServerUrlOrThrow() {
        try {
            String remoteUrl = getRemoteUrlProperty();
            if (!remoteUrl.isEmpty()) {
                return new URL(remoteUrl);
            }
            if (service == null) {
                throw new IllegalStateException("Appium service is null and no REMOTE_URL provided.");
            }
            return service.getUrl();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // --- your existing helpers (kept same names to avoid breaking callers) ---

    public Double getFormettedAmount(String amount) {
        return Double.parseDouble(amount.substring(1));
    }

    public void waitForTextToBePresent(WebElement element, String text, int timeoutSeconds, AppiumDriver driver) {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .until(ExpectedConditions.textToBePresentInElement(element, text));
    }

    public void waitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ignored) {}
    }

    public List<HashMap<String, String>> getJsonData(String jsonFilePath) throws IOException {
        String jsonContent = FileUtils.readFileToString(new File(jsonFilePath), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonContent, new TypeReference<List<HashMap<String, String>>>() {});
    }

    public String getScreenShotPath(String testCaseName, AppiumDriver driver) throws IOException {
        File source = driver.getScreenshotAs(OutputType.FILE);
        String destinationFile = System.getProperty("user.dir") + File.separator + "reports" + File.separator + testCaseName + ".png";
        FileUtils.copyFile(source, new File(destinationFile));
        return destinationFile;
    }
}
