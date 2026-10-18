package automation.pages;

import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import automation.utilities.CommonMethods;

/** Admin > Job: Job Titles, Pay Grades, Employment Status, Job Categories and Work Shifts. */
public class JobPage extends CommonMethods {

	public static final Logger logger = LogManager.getLogger(JobPage.class);

	public JobPage() {
		PageFactory.initElements(driver(), this);
	}

	private static final String FIELD = "//label[normalize-space()='%s']/ancestor::div[contains(@class,'oxd-input-group')]";

	private final By jobTab = By.xpath("//nav[@aria-label='Topbar Menu']//span[normalize-space()='Job']");
	private final By tableRows = By.cssSelector(".oxd-table-body .oxd-table-card");

	private WebElement field(String label) {
		return waitForElement(By.xpath(String.format(FIELD, label) + "//input | " + String.format(FIELD, label)
				+ "//textarea"));
	}

	/** Opens the Job drop-down in the top bar and picks one of its pages. */
	public void openJobPage(String page) {
		openJobPage(page, 1);
	}

	private void openJobPage(String page, int retriesLeft) {
		logger.info("Opening Admin > Job > " + page);
		By item = By.xpath("//ul[contains(@class,'oxd-dropdown-menu')]//a[normalize-space()='" + page + "']");
		WebElement menuItem;
		try {
			clickAndDraw(waitForElement(jobTab));
			menuItem = new WebDriverWait(driver(), Duration.ofSeconds(5))
					.until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(item));
		} catch (org.openqa.selenium.WebDriverException e) {
			// the tab click can land while the page is still re-rendering and then does not open the menu
			logger.info("Job menu did not open, clicking the Job tab again");
			clickAndDraw(waitForElement(jobTab));
			menuItem = waitForElement(item);
		}
		clickAndDraw(menuItem);
		waitTillLoadingScreenVanishes();
		// after a save the page can still be finishing its own redirect (e.g. to Edit Pay Grade) and swallow this click
		By title = By.cssSelector("h5.oxd-table-filter-title, h6.orangehrm-main-title");
		try {
			new WebDriverWait(driver(), Duration.ofSeconds(8)).ignoring(StaleElementReferenceException.class)
					.until(d -> d.findElements(title).stream().anyMatch(t -> t.getText().trim().equals(page)));
		} catch (TimeoutException e) {
			if (retriesLeft > 0) {
				logger.info("[" + page + "] list did not open, trying again");
				openJobPage(page, retriesLeft - 1);
			}
		}
	}

	public void enterValue(String label, String value) {
		safeSendKeys(field(label), value);
	}

	/**
	 * Replaces the value on an Edit form. The form loads its data after it appears and would overwrite anything typed
	 * before that, so wait until the field holds the saved value first.
	 */
	public void replaceValue(String label, String value) {
		waitTillLoadingScreenVanishes();
		try {
			new WebDriverWait(driver(), Duration.ofSeconds(ELEMENT_WAIT_TIMEOUT_SECONDS))
					.ignoring(StaleElementReferenceException.class)
					.until(d -> !field(label).getAttribute("value").isEmpty());
		} catch (TimeoutException e) {
			logger.warn("Field [" + label + "] was still empty after the Edit form loaded");
		}
		safeSendKeys(field(label), value);
	}

	public void enterTime(String label, String time) {
		safeSendKeys(field(label), time);
	}

	/**
	 * The Add forms live under ".../save<Record>"; a saved record moves the browser away from that URL. Used instead of
	 * the toast because Pay Grades shows no "Successfully Saved" toast, only an "Info" one on its Edit page.
	 */
	public void verifyRecordSaved() {
		boolean left = waitForUrlChange();
		softAssert().softAssertTrue(left, "The record was saved and the Add form was closed",
				"The Add form is still open. URL: " + driver().getCurrentUrl());
	}

	private boolean waitForUrlChange() {
		try {
			return new WebDriverWait(driver(), Duration.ofSeconds(ELEMENT_WAIT_TIMEOUT_SECONDS))
					.until(d -> !d.getCurrentUrl().contains("/save"));
		} catch (TimeoutException e) {
			return false;
		}
	}

	public void verifyRecordListed(String name, boolean shouldBeListed) {
		boolean listed;
		try {
			// the list re-renders after a save or a delete, so retry when a row goes stale while it is read
			listed = new WebDriverWait(driver(), Duration.ofSeconds(ELEMENT_WAIT_TIMEOUT_SECONDS))
					.ignoring(StaleElementReferenceException.class)
					.until(d -> isRowPresent(name) == shouldBeListed);
		} catch (TimeoutException e) {
			listed = isRowPresent(name) == shouldBeListed;
		}
		softAssert().softAssertTrue(listed, "[" + name + "] is " + (shouldBeListed ? "listed" : "not listed"),
				"[" + name + "] is " + (shouldBeListed ? "not listed" : "still listed"));
	}

	private boolean isRowPresent(String name) {
		return findRow(name) != null;
	}

	private WebElement rowOf(String name) {
		return new WebDriverWait(driver(), Duration.ofSeconds(ELEMENT_WAIT_TIMEOUT_SECONDS))
				.ignoring(StaleElementReferenceException.class).until(d -> findRow(name));
	}

	private static final String PAGINATION = "//nav[@aria-label='Pagination Navigation']";

	/** Looks for the row on every page of the list (records left by other runs can push a new one to page 2). */
	private WebElement findRow(String name) {
		goToPage(By.xpath(PAGINATION + "//button[normalize-space()='1'][not(contains(@class,'selected'))]"));
		By nextPage = By.xpath(PAGINATION
				+ "//button[contains(@class,'oxd-pagination-page-item--previous-next')][.//i[contains(@class,'bi-chevron-right')]]");
		while (true) {
			for (WebElement row : driver().findElements(tableRows)) {
				if (row.getText().contains(name)) {
					return row;
				}
			}
			if (!goToPage(nextPage)) {
				return null;
			}
		}
	}

	/** Clicks the pagination button if there is one and waits for the table to change. False when there is none. */
	private boolean goToPage(By button) {
		List<WebElement> buttons = driver().findElements(button);
		if (buttons.isEmpty()) {
			return false;
		}
		String before = driver().findElements(tableRows).isEmpty() ? ""
				: driver().findElements(tableRows).get(0).getText();
		clickAndDraw(buttons.get(0));
		try {
			new WebDriverWait(driver(), Duration.ofSeconds(5)).ignoring(StaleElementReferenceException.class)
					.until(d -> d.findElements(tableRows).isEmpty()
							|| !d.findElements(tableRows).get(0).getText().equals(before));
		} catch (TimeoutException e) {
			logger.info("Table did not change after the page click");
		}
		return true;
	}

	/** Clicks an icon button of the row; the table re-renders while loading, so retry when the row goes stale. */
	private void clickRowButton(String name, String iconClass) {
		for (int attempt = 1;; attempt++) {
			try {
				clickAndDraw(rowOf(name).findElement(By.xpath(".//button[.//i[contains(@class,'" + iconClass + "')]]")));
				return;
			} catch (org.openqa.selenium.WebDriverException e) {
				if (attempt == 3) {
					throw e;
				}
				logger.info("Row changed while clicking, retrying");
			}
		}
	}

	public void editRecord(String name) {
		clickRowButton(name, "bi-pencil-fill");
		waitTillLoadingScreenVanishes();
	}

	public void deleteRecord(String name) {
		clickRowButton(name, "bi-trash");
		clickAndDraw(waitForElement(By.xpath("//button[normalize-space()='Yes, Delete']")));
	}
}
