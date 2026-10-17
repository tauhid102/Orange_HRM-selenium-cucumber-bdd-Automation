package automation.step_definitions;

import automation.pages.LoginPage;
import automation.utilities.CommonMethods;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Login_SD extends CommonMethods {

	private final LoginPage loginPage = new LoginPage();

	@Given("User is on the login page")
	public void user_is_on_the_login_page() {
		loginPage.ensureOnLoginPage();
		loginPage.verifyLoginPageDisplayed();
	}

	@Given("User logs in as {string}")
	public void user_logs_in_as(String userRole) {
		loginPage.loginAs(userRole);
	}

	@Given("User is logged in as {string}")
	public void user_is_logged_in_as(String userRole) {
		loginPage.ensureLoggedIn(userRole);
		loginPage.verifyPageHeading("Dashboard");
	}

	@When("User logs in with username {string} and password {string}")
	public void user_logs_in_with_username_and_password(String username, String password) {
		loginPage.login(username, password);
	}

	@Then("Login error message {string} is displayed")
	public void login_error_message_is_displayed(String expectedMessage) {
		loginPage.verifyErrorMessage(expectedMessage);
	}

	@Then("User lands on the {string} page")
	public void user_lands_on_the_page(String pageHeading) {
		loginPage.verifyPageHeading(pageHeading);
	}

	@When("User logs out")
	public void user_logs_out() {
		loginPage.logoutFromApp();
	}
}
