package automation.utilities;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;


/**
 * Base class for page objects and step definitions. Holds the reusable wait, click, input and verification helpers
 * so pages only describe their own elements and behaviour.
 */
public class CommonMethods {

	/** ------------------ Global Variables -------------------- **/
	public static final int ELEMENT_WAIT_TIMEOUT_SECONDS = 15;
	public static final int PAGE_LOAD_TIMEOUT_SECONDS = 30;
	public static final int LOADING_SCREEN_TIMEOUT_SECONDS = 180;
	public static final Logger logger = LogManager.getLogger(CommonMethods.class);

	private static final ThreadLocal<SftAssert> softAssertPool = ThreadLocal.withInitial(SftAssert::new);
	private static final ThreadLocal<WebElement> lastHighlighted = new ThreadLocal<>();

	// ================================
	// 🔹 Shared Objects
	// ================================

	protected WebDriver driver() {
		return Driver.getDriver();
	}

	/** Soft assert for the scenario running on this thread. */
	public static SftAssert softAssert() {
		return softAssertPool.get();
	}

	/** Called from the @Before hook so every scenario starts with a clean soft assert. */
	public static void resetSoftAssert() {
		softAssertPool.set(new SftAssert());
	}

	// ================================
	// 🔹 Different Wait Methods
	// ================================

	/** Waits for document.readyState to be 'complete' and for jQuery AJAX calls (if jQuery is present). */
	public static void waitForPageAndAjaxToLoad() {
		WebDriver driver = Driver.getDriver();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(PAGE_LOAD_TIMEOUT_SECONDS));
		wait.until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
		try {
			Boolean jQueryDefined = (Boolean) ((JavascriptExecutor) driver)
					.executeScript("return typeof jQuery != 'undefined'");
			if (Boolean.TRUE.equals(jQueryDefined)) {
				wait.until(d -> (Boolean) ((JavascriptExecutor) d).executeScript("return jQuery.active == 0"));
			}
		} catch (Exception e) {
			logger.debug("jQuery not detected. Skipping AJAX wait.");
		}
	}

	public WebElement waitForElement(By locator) {
		WebElement element = new WebDriverWait(driver(), Duration.ofSeconds(ELEMENT_WAIT_TIMEOUT_SECONDS))
				.until(ExpectedConditions.visibilityOfElementLocated(locator));
		drawBorder(element);
		return element;
	}

	public WebElement waitForElement(WebElement element) {
		WebElement visible = new WebDriverWait(driver(), Duration.ofSeconds(ELEMENT_WAIT_TIMEOUT_SECONDS))
				.until(ExpectedConditions.visibilityOf(element));
		drawBorder(visible);
		return visible;
	}

	public WebElement waitForClickability(WebElement element) {
		return new WebDriverWait(driver(), Duration.ofSeconds(ELEMENT_WAIT_TIMEOUT_SECONDS))
				.until(ExpectedConditions.elementToBeClickable(element));
	}

	public boolean waitForUrlContains(String expectedSubstring) {
		try {
			return new WebDriverWait(driver(), Duration.ofSeconds(ELEMENT_WAIT_TIMEOUT_SECONDS))
					.until(ExpectedConditions.urlContains(expectedSubstring));
		} catch (TimeoutException e) {
			logger.warn("URL did not contain [" + expectedSubstring + "]. Current URL: " + driver().getCurrentUrl());
			return false;
		}
	}

	public boolean waitForTitle(String pageTitle) {
		try {
			return new WebDriverWait(driver(), Duration.ofSeconds(ELEMENT_WAIT_TIMEOUT_SECONDS))
					.until(ExpectedConditions.titleIs(pageTitle));
		} catch (TimeoutException e) {
			logger.warn("Page title was not [" + pageTitle + "]. Actual title: " + driver().getTitle());
			return false;
		}
	}

	/** Hard wait. Use only when there is nothing on the page to wait for. */
	public static void waitFor(int sec) {
		try {
			Thread.sleep(sec * 1000L);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	/** Waits up to LOADING_SCREEN_TIMEOUT_SECONDS for the page loader to disappear. Returns at once if there is none. */
	public void waitTillLoadingScreenVanishes() {
		By loadingImage = By.cssSelector(".oxd-form-loader, .oxd-loading-spinner");
		if (!isElementPresent(loadingImage)) {
			logger.info("No loading screen present, good to go");
			return;
		}
		logger.info("Loading screen is present, waiting for it to vanish");
		try {
			new WebDriverWait(driver(), Duration.ofSeconds(LOADING_SCREEN_TIMEOUT_SECONDS))
					.until(ExpectedConditions.invisibilityOfElementLocated(loadingImage));
			logger.info("Loading screen vanished");
		} catch (TimeoutException e) {
			logger.error(LogColor.RED + "Loading screen still showing after " + LOADING_SCREEN_TIMEOUT_SECONDS
					+ " seconds" + LogColor.RESET);
			throw e;
		}
	}

	// ================================
	// 🔹 Element State
	// ================================

	public boolean isElementPresent(By locator) {
		return !driver().findElements(locator).isEmpty();
	}

	public boolean isElementDisplayed(By locator) {
		List<WebElement> elements = driver().findElements(locator);
		return !elements.isEmpty() && elements.get(0).isDisplayed();
	}

	// ================================
	// 🔹 Actions
	// ================================

	public void clickAndDraw(WebElement element) {
		WebElement clickable = waitForClickability(element);
		drawBorder(clickable);
		clickable.click();
	}

	public void jsClick(WebElement element) {
		((JavascriptExecutor) driver()).executeScript("arguments[0].click();", element);
	}

	public void safeSendKeys(WebElement element, String text) {
		WebElement input = waitForElement(element);
		input.clear();
		if (text != null && !text.isEmpty()) {
			input.sendKeys(text);
		}
	}

	public void scrollIntoView(WebElement element) {
		((JavascriptExecutor) driver()).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
	}

	public void selectFromDropDownByVisibleText(WebElement dropdown, String optionText) {
		new Select(waitForElement(dropdown)).selectByVisibleText(optionText);
	}

	// ================================
	// 🔹 Read Values
	// ================================

	public static List<String> getElementsText(List<WebElement> elements) {
		List<String> texts = new ArrayList<>();
		for (WebElement element : elements) {
			texts.add(element.getText().trim());
		}
		return texts;
	}

	/** Turns a price label like "Item total: $39.98" or "$29.99" into a BigDecimal. */
	public static BigDecimal parseMoney(String text) {
		String amount = text.substring(text.indexOf('$') + 1).replace(",", "").trim();
		return new BigDecimal(amount);
	}

	// ================================
	// 🔹 Highlighting (makes screenshots easier to read)
	// ================================

	public void drawBorder(WebElement element) {
		if (!ConfigurationReader.getBoolean("highlightElements")) {
			return;
		}
		removeBorder();
		try {
			((JavascriptExecutor) driver()).executeScript("arguments[0].style.outline='3px solid red';", element);
			lastHighlighted.set(element);
		} catch (Exception e) {
			logger.debug("Unable to highlight element: " + e.getMessage());
		}
	}

	public void removeBorder() {
		WebElement previous = lastHighlighted.get();
		if (previous == null) {
			return;
		}
		try {
			((JavascriptExecutor) driver()).executeScript("arguments[0].style.outline='';", previous);
		} catch (Exception e) {
			// element is gone from the page after navigation, nothing to clean up
		} finally {
			lastHighlighted.remove();
		}
	}
}
