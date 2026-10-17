package automation.runners;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

/**
 * Main runner used by Maven (mvn test). Choose what to run with a tag expression, for example:
 * mvn test -Dcucumber.filter.tags="@smoke"
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = { "summary",
				"html:target/cucumber-reports/cucumber.html",
				"json:target/cucumber-reports/cucumber.json",
				"junit:target/cucumber-reports/cucumber.xml",
				"rerun:target/rerun.txt",
				"com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:",
				"automation.hooks.StepNameListener" },
		features = "src/test/resources/features",
		glue = { "automation.step_definitions", "automation.hooks" },
		tags = "not @ignore",
		monochrome = true,
		dryRun = false)
public class TestRunner {
}
