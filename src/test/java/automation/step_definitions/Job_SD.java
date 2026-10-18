package automation.step_definitions;

import automation.pages.AdminPage;
import automation.pages.JobPage;
import automation.utilities.CommonMethods;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Job_SD extends CommonMethods {

	private final JobPage jobPage = new JobPage();
	private final AdminPage adminPage = new AdminPage();

	private String recordName;

	private static String uniqueName() {
		return "Auto" + System.currentTimeMillis();
	}

	@When("User opens the Job page {string}")
	public void user_opens_the_job_page(String page) {
		jobPage.openJobPage(page);
	}

	@When("User enters a new unique name in the field {string}")
	public void user_enters_a_new_unique_name(String label) {
		recordName = uniqueName();
		jobPage.enterValue(label, recordName);
	}

	@When("User enters the same name again in the field {string}")
	public void user_enters_the_same_name_again(String label) {
		jobPage.enterValue(label, recordName);
	}

	@When("User saves the record")
	public void user_saves_the_record() {
		adminPage.save();
	}

	@Then("The record is saved")
	public void the_record_is_saved() {
		jobPage.verifyRecordSaved();
	}

	@When("User opens the record for editing")
	public void user_opens_the_record_for_editing() {
		jobPage.editRecord(recordName);
	}

	@When("User renames the record in the field {string}")
	public void user_renames_the_record(String label) {
		recordName = uniqueName();
		jobPage.replaceValue(label, recordName);
	}

	@When("User deletes the record")
	public void user_deletes_the_record() {
		jobPage.deleteRecord(recordName);
	}

	@Then("The list shows the record")
	public void the_list_shows_the_record() {
		jobPage.verifyRecordListed(recordName, true);
	}

	@Then("The list does not show the record")
	public void the_list_does_not_show_the_record() {
		jobPage.verifyRecordListed(recordName, false);
	}
}
