package automation.utilities;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Creates and owns the WebDriver. The driver is kept in a ThreadLocal so every thread gets its own browser, which
 * keeps the framework ready for parallel execution. Browser drivers are resolved by Selenium Manager, so no
 * driver executables need to be downloaded by hand.
 */
public class Driver {

	private static final ThreadLocal<WebDriver> driverPool = new ThreadLocal<>();
	public static final Logger logger = LogManager.getLogger(Driver.class);

	// default downloads path
	public static final String downloadFolderPath = System.getProperty("user.dir") + File.separator + "downloads";

	private Driver() {
	}

	public static WebDriver getDriver() {
		if (driverPool.get() == null) {
			String browser = ConfigurationReader.getProperty("browser", "chrome").trim().toLowerCase();
			logger.info(LogColor.Purple + "Launching browser: " + browser + LogColor.RESET);
			driverPool.set(createDriver(browser));
		}
		return driverPool.get();
	}

	public static boolean hasDriver() {
		return driverPool.get() != null;
	}

	public static void closeDriver() {
		WebDriver driver = driverPool.get();
		if (driver != null) {
			try {
				driver.quit();
			} catch (Exception e) {
				logger.warn("Driver quit raised an exception: " + e.getMessage());
			} finally {
				driverPool.remove();
			}
		}
	}

	/**
	 * Makes sure a browser is open. The first call launches it, applies timeouts and opens the application URL; later
	 * calls reuse the running browser and keep its session, so a logged in user stays logged in between scenarios.
	 */
	public static void browserSetup() {
		if (isBrowserAlive()) {
			logger.info(LogColor.Purple + " Reusing the open browser " + LogColor.RESET);
			return;
		}
		logger.info(LogColor.Purple + " Browser Setup " + LogColor.RESET);
		closeDriver();
		WebDriver driver = getDriver();
		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigurationReader.getInt("pageLoadTimeout", 30)));
		if (ConfigurationReader.getProperty("browser", "chrome").toLowerCase().contains("headless")) {
			// a headless browser has no screen to maximize to, and a small window collapses the OrangeHRM sidebar
			driver.manage().window().setSize(new Dimension(1920, 1080));
		} else {
			driver.manage().window().maximize();
		}
		openApplication();
	}

	/** Navigates to the application URL. */
	public static void openApplication() {
		String url = ConfigurationReader.getProperty("url");
		getDriver().get(url);
		logger.info("URL Launched : " + url);
		CommonMethods.waitForPageAndAjaxToLoad();
	}

	/** True when a browser was launched for this thread and is still responding. */
	public static boolean isBrowserAlive() {
		if (!hasDriver()) {
			return false;
		}
		try {
			driverPool.get().getWindowHandle();
			return true;
		} catch (Exception e) {
			logger.warn("Open browser is not responding, a new one will be launched.");
			return false;
		}
	}

	private static WebDriver createDriver(String browser) {
		switch (browser) {
		case "chrome":
			return new ChromeDriver(configureChromium(new ChromeOptions(), false, "--incognito"));
		case "chrome-headless":
			return new ChromeDriver(configureChromium(new ChromeOptions(), true, "--incognito"));
		case "edge":
			return new EdgeDriver(configureChromium(new EdgeOptions(), false, "--inprivate"));
		case "edge-headless":
			return new EdgeDriver(configureChromium(new EdgeOptions(), true, "--inprivate"));
		case "firefox":
			return new FirefoxDriver(configureFirefox(false));
		case "firefox-headless":
			return new FirefoxDriver(configureFirefox(true));
		default:
			throw new IllegalArgumentException("Unsupported browser: " + browser
					+ ". Use chrome, chrome-headless, edge, edge-headless, firefox or firefox-headless.");
		}
	}

	/** Chrome and Edge share the Chromium option set, so they are configured in one place. */
	private static <T extends ChromiumOptions<?>> T configureChromium(T options, boolean headless,
			String privateModeArgument) {
		Map<String, Object> prefs = new HashMap<>();
		prefs.put("download.default_directory", downloadFolderPath);
		prefs.put("download.prompt_for_download", false);
		prefs.put("profile.default_content_settings.popups", 0);
		prefs.put("profile.default_content_setting_values.automatic_downloads", 1);
		// Stops the browser's "password found in a data breach" popup from covering the page
		prefs.put("credentials_enable_service", false);
		prefs.put("profile.password_manager_enabled", false);
		prefs.put("profile.password_manager_leak_detection", false);
		options.setExperimentalOption("prefs", prefs);

		options.addArguments(privateModeArgument);
		options.addArguments("--disable-notifications");
		options.addArguments("--disable-gpu");
		options.addArguments("--window-size=1920,1080");

		if (headless) {
			options.addArguments("--headless=new");
			options.addArguments("--no-sandbox");
			options.addArguments("--disable-dev-shm-usage");
		}
		return options;
	}

	private static FirefoxOptions configureFirefox(boolean headless) {
		FirefoxOptions options = new FirefoxOptions();
		options.addPreference("browser.download.folderList", 2);
		options.addPreference("browser.download.dir", downloadFolderPath);
		options.addPreference("signon.rememberSignons", false);
		options.addArguments("-private");
		if (headless) {
			options.addArguments("-headless");
			options.addArguments("--width=1920");
			options.addArguments("--height=1080");
		}
		return options;
	}
}
