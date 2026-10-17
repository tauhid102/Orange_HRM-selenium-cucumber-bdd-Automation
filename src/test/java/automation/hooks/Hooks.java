package automation.hooks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;

import automation.utilities.CommonMethods;
import automation.utilities.ConfigurationReader;
import automation.utilities.Driver;
import automation.utilities.LogColor;
import automation.utilities.ScreenshotUtil;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.Scenario;

/**
 * Cucumber hooks. One browser is launched for the whole run and reused by every UI scenario; scenarios log in only
 * when they are not already logged in. Scenarios tagged @api never open a browser.
 */
public class Hooks {

	public static final Logger logger = LogManager.getLogger(Hooks.class);

	@BeforeAll
	public static void beforeAll() {
		logger.info(LogColor.Indigo + " @BeforeAll hook - runs once before the first scenario " + LogColor.RESET);
	}

	@Before(order = 0)
	public void scenarioName(Scenario scenario) {
		logger.info(LogColor.DarkGreen + "================================================================" + LogColor.RESET);
		logger.info(LogColor.DarkGreen + "Scenario Title: " + scenario.getName().trim() + LogColor.RESET);
		logger.info(LogColor.DarkGreen + "Tags: " + scenario.getSourceTagNames() + LogColor.RESET);
		logger.info(LogColor.DarkGreen + "================================================================" + LogColor.RESET);
		CommonMethods.resetSoftAssert();
	}

	@Before(value = "not @api", order = 1)
	public void launchBrowser() {
		logger.info(LogColor.Indigo + " @Before hook - launching browser if not already open " + LogColor.RESET);
		Driver.browserSetup();
	}

	@BeforeStep
	public void beforeStep() {
		logger.info(LogColor.Indigo + " @BeforeStep hook " + LogColor.RESET);
	}

	@AfterStep("not @api")
	public void takeScreenshot(Scenario scenario) {
		if (scenario.isFailed() || ConfigurationReader.getBoolean("screenshotOnEveryStep")) {
			byte[] screenshot = ScreenshotUtil.takeScreenshot();
			if (screenshot != null) {
				scenario.attach(screenshot, "image/png", scenario.getName());
			}
		}
	}

	/** Runs before closeBrowser (higher order runs first), so soft assert failures are reported first. */
	@After(order = 10)
	public void after(Scenario scenario) {
		try {
			CommonMethods.softAssert().assertAll();
		} finally {
			logger.info(LogColor.DarkGreen + "================================================================" + LogColor.RESET);
			logger.info(LogColor.Indigo + scenario.getName() + " :- Status - " + scenario.getStatus() + LogColor.RESET);
			logger.info(LogColor.DarkGreen + "================================================================" + LogColor.RESET);
		}
	}

	/**
	 * The browser stays open for the next scenario. Only the cart is cleared so scenarios do not see each other's
	 * products; if that fails the browser is closed and the next scenario launches a fresh one.
	 */
	@After(value = "not @api", order = 0)
	public void resetBrowserState() {
		try {
			((JavascriptExecutor) Driver.getDriver()).executeScript("window.localStorage.removeItem('cart-contents');");
		} catch (Exception e) {
			logger.warn("Could not reset browser state, closing the browser: " + e.getMessage());
			Driver.closeDriver();
		}
	}

	@AfterAll
	public static void afterAll() {
		logger.info(LogColor.Indigo + " @AfterAll hook - runs once after the last scenario " + LogColor.RESET);
		Driver.closeDriver();
	}
}
