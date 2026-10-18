package automation.step_definitions;

import automation.pages.AdminPage;
import automation.utilities.CommonMethods;
import automation.utilities.DataGenerator;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Admin_SD extends CommonMethods {

	private final AdminPage adminPage = new AdminPage();

	private String username;

	@When("User navigates to the Admin menu")
	public void user_navigates_to_the_admin_menu() {
		adminPage.openAdminMenu();
	}

	@Then("User sees the {string} section")
	public void user_sees_the_section(String title) {
		adminPage.verifySectionTitle(title);
	}

	@When("User clicks the Add button")
	public void user_clicks_the_add_button() {
		adminPage.clickAdd();
	}

	@When("User fills the new user form with role {string}, employee starting with {string} and status {string}")
	public void user_fills_the_new_user_form(String role, String employeeSearch, String status) {
		username = DataGenerator.username();
		adminPage.selectOption("User Role", role);
		adminPage.selectEmployee(employeeSearch);
		adminPage.selectOption("Status", status);
		adminPage.enterUsername(username);
		adminPage.enterPassword(DataGenerator.password());
	}

	@When("User saves the new user")
	public void user_saves_the_new_user() {
		adminPage.save();
	}

	@Then("Success message {string} is displayed")
	public void success_message_is_displayed(String message) {
		adminPage.verifySuccessMessage(message);
	}

	// ---- Add user form ----

	@When("User selects the user role {string}")
	public void user_selects_the_user_role(String role) {
		adminPage.selectOption("User Role", role);
	}

	@When("User selects an employee starting with {string}")
	public void user_selects_an_employee_starting_with(String text) {
		adminPage.selectEmployee(text);
	}

	@When("User types the employee name {string} without selecting a suggestion")
	public void user_types_the_employee_name(String name) {
		adminPage.typeEmployeeName(name);
	}

	@When("User selects the status {string}")
	public void user_selects_the_status(String status) {
		adminPage.selectOption("Status", status);
	}

	@When("User enters the username {string}")
	public void user_enters_the_username(String name) {
		adminPage.enterUsername(name);
	}

	@When("User enters the password {string} and confirm password {string}")
	public void user_enters_the_passwords(String password, String confirmPassword) {
		adminPage.enterPasswords(password, confirmPassword);
	}

	@When("User clicks the Cancel button")
	public void user_clicks_the_cancel_button() {
		adminPage.clickCancel();
	}

	@Then("The field {string} shows the error {string}")
	public void the_field_shows_the_error(String field, String error) {
		adminPage.verifyFieldError(field, error);
	}

	// ---- Search ----

	@Then("The users table is displayed with the columns {string}")
	public void the_users_table_is_displayed_with_the_columns(String columns) {
		adminPage.verifyTableColumns(columns);
	}

	@When("User searches for the username {string}")
	public void user_searches_for_the_username(String name) {
		adminPage.searchUser(name);
	}

	@When("User searches for the user role {string}")
	public void user_searches_for_the_user_role(String role) {
		adminPage.searchByOption("User Role", role);
	}

	@When("User searches for the status {string}")
	public void user_searches_for_the_status(String status) {
		adminPage.searchByOption("Status", status);
	}

	@When("User clicks the Reset button")
	public void user_clicks_the_reset_button() {
		adminPage.clickReset();
	}

	@Then("The records message {string} is displayed")
	public void the_records_message_is_displayed(String message) {
		adminPage.verifyRecordsMessage(message);
	}

	@Then("The table lists the user {string}")
	public void the_table_lists_the_user(String name) {
		adminPage.verifyUsernameListed(name);
	}

	@Then("Every listed user has the role {string}")
	public void every_listed_user_has_the_role(String role) {
		adminPage.verifyEveryRowHas(2, role, "User role");
	}

	@Then("Every listed user has the status {string}")
	public void every_listed_user_has_the_status(String status) {
		adminPage.verifyEveryRowHas(4, status, "Status");
	}

	@Then("The search fields are cleared")
	public void the_search_fields_are_cleared() {
		adminPage.verifySearchFieldsCleared();
	}

	// ---- Create, edit and delete ----

	@Given("A new user exists with role {string} and status {string}")
	public void a_new_user_exists(String role, String status) {
		adminPage.clickAdd();
		user_fills_the_new_user_form(role, "a", status);
		adminPage.save();
		adminPage.verifySuccessMessage("Successfully Saved");
	}

	@When("User edits the new user and sets the status {string}")
	public void user_edits_the_new_user(String status) {
		adminPage.searchUser(username);
		adminPage.editUser(username);
		adminPage.verifySectionTitle("Edit User");
		adminPage.selectOption("Status", status);
		adminPage.save();
	}

	@When("User deletes the new user")
	public void user_deletes_the_new_user() {
		adminPage.searchUser(username);
		adminPage.deleteUser(username);
	}

	@When("User searches for the new user by username")
	public void user_searches_for_the_new_user_by_username() {
		adminPage.searchUser(username);
	}

	@Then("The new user is available in the table with role {string} and status {string}")
	public void the_new_user_is_available_in_the_table(String role, String status) {
		adminPage.verifyUserInTable(username, role, status);
	}
}
