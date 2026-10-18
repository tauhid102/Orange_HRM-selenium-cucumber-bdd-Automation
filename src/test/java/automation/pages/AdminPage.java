package automation.pages;

import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import automation.utilities.CommonMethods;

/** Admin > User Management > Users: the user list and the Add User form. */
public class AdminPage extends CommonMethods {

	public static final Logger logger = LogManager.getLogger(AdminPage.class);

	public AdminPage() {
		PageFactory.initElements(driver(), this);
	}

	@FindBy(xpath = "//a[contains(@class,'oxd-main-menu-item')][normalize-space()='Admin']")
	public WebElement adminMenu;

	@FindBy(css = "h5.oxd-table-filter-title, h6.orangehrm-main-title")
	public WebElement sectionTitle;

	@FindBy(xpath = "//button[normalize-space()='Add']")
	public WebElement addButton;

	@FindBy(xpath = "//button[normalize-space()='Search']")
	public WebElement searchButton;

	@FindBy(css = "button[type='submit']")
	public WebElement saveButton;

	@FindBy(css = ".oxd-toast")
	public WebElement toast;

	// Form fields are located by their label so the locators survive layout changes.
	private static final String FIELD = "//label[normalize-space()='%s']/ancestor::div[contains(@class,'oxd-input-group')]";

	private final By employeeSuggestion = By.cssSelector(".oxd-autocomplete-option span");
	private final By tableRows = By.cssSelector(".oxd-table-body .oxd-table-card");

	private String selectedEmployee;

	private WebElement input(String label) {
		return waitForElement(By.xpath(String.format(FIELD, label) + "//input"));
	}

	private WebElement dropdown(String label) {
		return waitForElement(
				By.xpath(String.format(FIELD, label) + "//div[contains(@class,'oxd-select-text')]"));
	}

	public void openAdminMenu() {
		logger.info("Opening the Admin menu");
		clickAndDraw(adminMenu);
		waitTillLoadingScreenVanishes();
	}

	public void verifySectionTitle(String expectedTitle) {
		String actualTitle = waitForElement(sectionTitle).getText().trim();
		softAssert().softAssertEquals(actualTitle, expectedTitle, "Section title");
	}

	public void clickAdd() {
		clickAndDraw(addButton);
		waitTillLoadingScreenVanishes();
	
	}

	public void selectOption(String label, String option) {
		clickAndDraw(dropdown(label));
		By optionLocator = By.xpath("//div[@role='listbox']//span[normalize-space()='" + option + "']");
		clickAndDraw(waitForElement(optionLocator));
	}

	/** Types the text in Employee Name and picks the first employee the autocomplete suggests. */
	public void selectEmployee(String searchText) {
		safeSendKeys(input("Employee Name"), searchText);
		WebElement suggestion = new WebDriverWait(driver(), Duration.ofSeconds(ELEMENT_WAIT_TIMEOUT_SECONDS))
				.until(d -> {
					List<WebElement> options = d.findElements(employeeSuggestion);
					if (options.isEmpty()) {
						return null;
					}
					String text = options.get(0).getText();
					return text.startsWith("Searching") || text.startsWith("No Records") ? null : options.get(0);
				});
		selectedEmployee = suggestion.getText().trim();
		logger.info("Selected employee [" + selectedEmployee + "]");
		clickAndDraw(suggestion);
	}

	public void enterUsername(String username) {
		safeSendKeys(input("Username"), username);
	}

	public void enterPassword(String password) {
		safeSendKeys(input("Password"), password);
		safeSendKeys(input("Confirm Password"), password);
	}

	public void save() {
		// the form is covered by a loader while the page is still fetching data, and a click would hit the loader
		waitTillLoadingScreenVanishes();
		clickAndDraw(saveButton);
	}

	public void verifySuccessMessage(String expectedMessage) {
		// more than one toast can be on screen (e.g. an "Info" toast next to the "Success" one), so look at all of them
		By toasts = By.cssSelector(".oxd-toast");
		boolean found = false;
		try {
			found = new WebDriverWait(driver(), Duration.ofSeconds(ELEMENT_WAIT_TIMEOUT_SECONDS))
					.ignoring(StaleElementReferenceException.class).until(d -> {
						for (WebElement t : d.findElements(toasts)) {
							if (t.getText().contains(expectedMessage)) {
								return true;
							}
						}
						return false;
					});
		} catch (TimeoutException e) {
			logger.warn("No toast contained [" + expectedMessage + "]");
		}
		String actualMessage = "";
		for (WebElement t : driver().findElements(toasts)) {
			actualMessage = actualMessage.isEmpty() ? t.getText() : actualMessage + " | " + t.getText();
		}
		softAssert().softAssertTrue(found,
				"Success message displayed: " + expectedMessage, "Success message [" + expectedMessage
						+ "] not displayed. Actual: " + actualMessage);
	}

	/** Types the username in the search field of the user list and clicks Search. */
	public void searchUser(String username) {
		waitForElement(sectionTitle);
		safeSendKeys(input("Username"), username);
		clickSearchAndWait();
	}

	public void searchByOption(String label, String option) {
		waitForElement(sectionTitle);
		selectOption(label, option);
		clickSearchAndWait();
	}

	public void clickReset() {
		clickAndDraw(driver().findElement(By.xpath("//button[normalize-space()='Reset']")));
		waitTillLoadingScreenVanishes();
	}

	public void clickCancel() {
		clickAndDraw(waitForElement(By.xpath("//button[normalize-space()='Cancel']")));
		waitTillLoadingScreenVanishes();
	}

	private String recordsText() {
		List<WebElement> labels = driver().findElements(By.xpath(
				"//div[contains(@class,'orangehrm-horizontal-padding')]//span[contains(normalize-space(),'Record')]"));
		return labels.isEmpty() ? "" : labels.get(0).getText().trim();
	}

	private String listSnapshot() {
		StringBuilder snapshot = new StringBuilder(recordsText());
		for (WebElement row : driver().findElements(tableRows)) {
			snapshot.append('|').append(row.getText());
		}
		return snapshot.toString();
	}

	/** Clicks Search and waits until the list shows the new result (the old list stays on screen until then). */
	private void clickSearchAndWait() {
		String before = listSnapshot();
		clickAndDraw(searchButton);
		try {
			new WebDriverWait(driver(), Duration.ofSeconds(5)).until(d -> !listSnapshot().equals(before));
		} catch (TimeoutException e) {
			logger.info("Search result is the same as the list before the search");
		}
		waitTillLoadingScreenVanishes();
	}

	public void verifyRecordsMessage(String expected) {
		String actual = "";
		try {
			new WebDriverWait(driver(), Duration.ofSeconds(ELEMENT_WAIT_TIMEOUT_SECONDS))
					.until(d -> recordsText().contains(expected));
		} catch (TimeoutException e) {
			logger.warn("Records message was not [" + expected + "]");
		}
		actual = recordsText();
		softAssert().softAssertTrue(actual.contains(expected), "Records message [" + actual + "]",
				"Records message expected [" + expected + "] but was [" + actual + "]");
	}

	public void verifyTableColumns(String commaSeparatedColumns) {
		String header = waitForElement(By.cssSelector(".oxd-table-header")).getText();
		for (String column : commaSeparatedColumns.split(",")) {
			String name = column.trim();
			softAssert().softAssertTrue(header.contains(name), "Column [" + name + "] is shown",
					"Column [" + name + "] missing. Header: " + header);
		}
	}

	/** Cell positions in a table row: 0 checkbox, 1 username, 2 user role, 3 employee name, 4 status, 5 actions. */
	public void verifyEveryRowHas(int cellIndex, String expected, String columnName) {
		List<WebElement> rows = driver().findElements(tableRows);
		softAssert().softAssertTrue(!rows.isEmpty(), "Search returned rows", "Search returned no rows");
		for (WebElement row : rows) {
			String actual = row.findElements(By.cssSelector("div[role='cell']")).get(cellIndex).getText().trim();
			softAssert().softAssertEquals(actual, expected, columnName + " of every listed user");
		}
	}

	public void verifyUsernameListed(String username) {
		boolean listed;
		try {
			// the table re-renders after a search, so retry when a row goes stale while it is read
			listed = new WebDriverWait(driver(), Duration.ofSeconds(ELEMENT_WAIT_TIMEOUT_SECONDS))
					.ignoring(StaleElementReferenceException.class)
					.until(d -> d.findElements(tableRows).stream().anyMatch(r -> r
							.findElements(By.cssSelector("div[role='cell']")).get(1).getText().trim().equals(username)));
		} catch (TimeoutException e) {
			listed = false;
		}
		softAssert().softAssertTrue(listed, "User [" + username + "] is listed", "User [" + username + "] is not listed");
	}

	public void verifySearchFieldsCleared() {
		String username = input("Username").getAttribute("value");
		String role = dropdown("User Role").getText().trim();
		String status = dropdown("Status").getText().trim();
		softAssert().softAssertEquals(username, "", "Username search field after reset");
		softAssert().softAssertEquals(role, "-- Select --", "User Role filter after reset");
		softAssert().softAssertEquals(status, "-- Select --", "Status filter after reset");
	}

	// ---- Add / Edit user form ----

	public void typeEmployeeName(String text) {
		safeSendKeys(input("Employee Name"), text);
	}

	public void enterPasswords(String password, String confirmPassword) {
		safeSendKeys(input("Password"), password);
		safeSendKeys(input("Confirm Password"), confirmPassword);
	}

	public void verifyFieldError(String label, String expectedError) {
		By error = By.xpath(String.format(FIELD, label)
				+ "//span[contains(@class,'oxd-input-field-error-message')]");
		// some errors (e.g. "Already exists") arrive from the server after an earlier one, so wait for the expected text
		String actual = "(no error shown)";
		try {
			new WebDriverWait(driver(), Duration.ofSeconds(ELEMENT_WAIT_TIMEOUT_SECONDS)).until(d -> {
				List<WebElement> shown = d.findElements(error);
				return !shown.isEmpty() && shown.get(0).getText().trim().equals(expectedError);
			});
		} catch (TimeoutException e) {
			logger.warn("Error under [" + label + "] was not [" + expectedError + "]");
		}
		List<WebElement> shown = driver().findElements(error);
		if (!shown.isEmpty()) {
			actual = shown.get(0).getText().trim();
		}
		softAssert().softAssertEquals(actual, expectedError, "Error under [" + label + "]");
	}

	// ---- Edit / Delete user ----

	private WebElement rowOf(String username) {
		return new WebDriverWait(driver(), Duration.ofSeconds(ELEMENT_WAIT_TIMEOUT_SECONDS)).until(
				d -> d.findElements(tableRows).stream().filter(r -> r.getText().contains(username)).findFirst().orElse(null));
	}

	public void editUser(String username) {
		WebElement row = rowOf(username);
		clickAndDraw(row.findElement(By.xpath(".//button[.//i[contains(@class,'bi-pencil-fill')]]")));
		waitTillLoadingScreenVanishes();
	}

	public void deleteUser(String username) {
		WebElement row = rowOf(username);
		clickAndDraw(row.findElement(By.xpath(".//button[.//i[contains(@class,'bi-trash')]]")));
		clickAndDraw(waitForElement(By.xpath("//button[normalize-space()='Yes, Delete']")));
	}

	/** Checks the search result table holds the new user with the chosen role, employee and status. */
	public void verifyUserInTable(String username, String role, String status) {
		// the old list stays on screen until the search result replaces it, so wait for the row of the new user
		String rowText;
		try {
			WebElement row = new WebDriverWait(driver(), Duration.ofSeconds(ELEMENT_WAIT_TIMEOUT_SECONDS))
					.until(d -> d.findElements(tableRows).stream().filter(r -> r.getText().contains(username))
							.findFirst().orElse(null));
			drawBorder(row);
			rowText = row.getText();
		} catch (TimeoutException e) {
			rowText = "";
		}
		softAssert().softAssertTrue(rowText.contains(username), "User [" + username + "] is listed",
				"User [" + username + "] not found in the list. Row: " + rowText);
		softAssert().softAssertTrue(rowText.contains(role), "Role [" + role + "] is listed",
				"Role [" + role + "] not found. Row: " + rowText);
		softAssert().softAssertTrue(rowText.contains(status), "Status [" + status + "] is listed",
				"Status [" + status + "] not found. Row: " + rowText);
		// the suggestion shows "First Middle Last" but the list shows only "First Last"
		String[] nameParts = selectedEmployee == null ? new String[0] : selectedEmployee.split("\\s+");
		String employeeName = nameParts.length < 2 ? String.join(" ", nameParts)
				: nameParts[0] + " " + nameParts[nameParts.length - 1];
		softAssert().softAssertTrue(rowText.contains(employeeName), "Employee [" + employeeName + "] is listed",
				"Employee [" + employeeName + "] not found. Row: " + rowText);
	}
}
