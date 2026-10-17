package automation.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import automation.utilities.CommonMethods;
import automation.utilities.ConfigurationReader;
import automation.utilities.Driver;

public class LoginPage extends CommonMethods {

	public static final Logger logger = LogManager.getLogger(LoginPage.class);

	public LoginPage() {
		PageFactory.initElements(driver(), this);
	}

	@FindBy(name = "username")
	public WebElement usernameField;

	@FindBy(name = "password")
	public WebElement passwordField;

	@FindBy(css = "button[type='submit']")
	public WebElement loginButton;

	@FindBy(css = ".oxd-alert-content-text")
	public WebElement errorMessage;

	@FindBy(css = ".oxd-topbar-header-breadcrumb-module")
	public WebElement pageHeading;

	@FindBy(css = ".oxd-userdropdown-tab")
	public WebElement userDropdown;

	@FindBy(xpath = "//a[normalize-space()='Logout']")
	public WebElement logoutLink;

	private static final String SESSION_COOKIE = "orangehrm";
	private static final String LOGIN_PATH = "auth/login";
	private static final String DASHBOARD_PATH = "dashboard/index";

	// By Locator
	By loginButtonLocator = By.cssSelector("button[type='submit']");

	/** Logs in with the user stored under the given role key in Configuration.properties (e.g. "standard"). */
	public void loginAs(String userRole) {
		String username = ConfigurationReader.getProperty(userRole);
		if (username == null) {
			throw new IllegalArgumentException("No user configured for role: " + userRole);
		}
		login(username, ConfigurationReader.getProperty("password"));
	}

	/** True when the browser holds a logged in session (OrangeHRM keeps it in the "orangehrm" cookie). */
	public boolean isLoggedIn() {
		return driver().manage().getCookieNamed(SESSION_COOKIE) != null;
	}

	/**
	 * Logs in only when needed. Already logged in: just opens the dashboard (the cookie holds a session id, not the
	 * user name, so the user cannot be compared). Not logged in: uses the normal login.
	 */
	public void ensureLoggedIn(String userRole) {
		String username = ConfigurationReader.getProperty(userRole);
		if (username == null) {
			throw new IllegalArgumentException("No user configured for role: " + userRole);
		}
		if (isLoggedIn()) {
			logger.info("Already logged in, skipping login");
			driver().get(ConfigurationReader.getProperty("url").replace(LOGIN_PATH, DASHBOARD_PATH));
			return;
		}
		Driver.openApplication();
		login(username, ConfigurationReader.getProperty("password"));
	}

	/** Makes sure the login form is showing: ends any open session and reloads the application URL. */
	public void ensureOnLoginPage() {
		if (isLoggedIn()) {
			logout();
		}
		Driver.openApplication();
	}

	private void logout() {
		logger.info("Ending the current session");
		driver().manage().deleteAllCookies();
		driver().navigate().refresh();
	}

	public void login(String username, String password) {
		logger.info("Logging in as [" + username + "]");
		safeSendKeys(usernameField, username);
		safeSendKeys(passwordField, password);
		clickAndDraw(loginButton);
	}

	public void verifyLoginPageDisplayed() {
		boolean onLoginPage = waitForElement(loginButtonLocator).isDisplayed()
				&& waitForTitle(ConfigurationReader.getProperty("HomePageTitle"));
		softAssert().softAssertTrue(onLoginPage, "User is on the Login Page", "User is not on the Login Page");
	}

	/** Checks the heading in the top bar after login (e.g. "Dashboard"). */
	public void verifyPageHeading(String expectedHeading) {
		String actualHeading = waitForElement(pageHeading).getText().trim();
		softAssert().softAssertEquals(actualHeading, expectedHeading, "Page heading");
	}

	/** Logs out through the user dropdown in the top bar. */
	public void logoutFromApp() {
		clickAndDraw(waitForClickability(userDropdown));
		clickAndDraw(waitForClickability(logoutLink));
	}

	public void verifyErrorMessage(String expectedMessage) {
		String actualMessage = waitForElement(errorMessage).getText().trim();
		softAssert().softAssertEquals(actualMessage, expectedMessage, "Login error message");
	}
}
