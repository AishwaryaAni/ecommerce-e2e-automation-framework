package com.qa.shop.driver;

import com.qa.shop.config.ConfigReader;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * One browser per test thread (ThreadLocal), plus two stability measures for a real,
 * ad-funded public shop:
 *  1. Ad / tracking / consent hosts are blackholed at DNS level, which removes the
 *     interstitial ads and overlays that randomly intercept clicks.
 *  2. Page-load strategy EAGER: tests continue once the DOM is ready instead of
 *     waiting for every third-party script.
 * Each thread also gets its own download folder, so parallel invoice downloads never clash.
 */
public final class DriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<Path> DOWNLOAD_DIR = new ThreadLocal<>();

    private static final List<String> BLOCKED_HOSTS = List.of(
            "googlesyndication.com", "doubleclick.net", "googleadservices.com", "adservice.google.com",
            "google-analytics.com", "googletagmanager.com", "googletagservices.com",
            "fundingchoicesmessages.google.com", "adtrafficquality.google", "amazon-adsystem.com");

    private DriverFactory() { }

    public static void start() {
        String browser = ConfigReader.get("browser").toLowerCase();
        boolean headless = ConfigReader.getBoolean("headless");
        Path downloads = newDownloadDir();

        WebDriver driver = switch (browser) {
            case "chrome" -> new ChromeDriver(chromeOptions(headless, downloads));
            case "firefox" -> new FirefoxDriver(firefoxOptions(headless, downloads));
            default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
        };
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getInt("page.load.timeout")));
        driver.manage().window().setSize(new Dimension(1920, 1080));

        DRIVER.set(driver);
        DOWNLOAD_DIR.set(downloads);
    }

    public static WebDriver get() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("No browser for this thread — call DriverFactory.start() first");
        }
        return driver;
    }

    public static boolean isStarted() {
        return DRIVER.get() != null;
    }

    public static Path downloadDir() {
        return DOWNLOAD_DIR.get();
    }

    public static void quit() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
        }
        DRIVER.remove();
        DOWNLOAD_DIR.remove();
    }

    private static ChromeOptions chromeOptions(boolean headless, Path downloads) {
        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        if (headless) {
            options.addArguments("--headless=new");
        }
        StringBuilder rules = new StringBuilder();
        for (String host : BLOCKED_HOSTS) {
            rules.append("MAP ").append(host).append(" 127.0.0.1, MAP *.").append(host).append(" 127.0.0.1, ");
        }
        options.addArguments(
                "--host-resolver-rules=" + rules.substring(0, rules.length() - 2),
                "--disable-notifications", "--no-sandbox", "--disable-dev-shm-usage");
        options.setExperimentalOption("prefs", Map.of(
                "download.default_directory", downloads.toAbsolutePath().toString(),
                "download.prompt_for_download", false,
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false));
        return options;
    }

    private static FirefoxOptions firefoxOptions(boolean headless, Path downloads) {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("browser.download.folderList", 2);
        profile.setPreference("browser.download.dir", downloads.toAbsolutePath().toString());
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "text/plain,application/octet-stream");
        FirefoxOptions options = new FirefoxOptions().setProfile(profile);
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        if (headless) {
            options.addArguments("-headless");
        }
        return options;
    }

    private static Path newDownloadDir() {
        try {
            Path base = Path.of("target", "downloads");
            Files.createDirectories(base);
            return Files.createTempDirectory(base, "thread-");
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create download directory", e);
        }
    }
}
