package automation.runners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import automation.utilities.LogColor;

/**
 * Re-runs only the scenarios that failed in the last TestRunner run (listed in target/rerun.txt):
 * mvn test -Dtest=FailedTestRunner
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = { "summary",
				"html:target/cucumber-reports/rerun.html",
				"com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:",
				"automation.hooks.StepNameListener" },
		features = "@target/rerun.txt",
		glue = { "automation.step_definitions", "automation.hooks" },
		monochrome = true)
public class FailedTestRunner {

	public static final Logger logger = LogManager.getLogger(FailedTestRunner.class);

	@BeforeClass
	public static void globalSetup() {
		logger.info(LogColor.ThinnerPurple + " @BeforeClass - FailedTestRunner running one time" + LogColor.RESET);
	}

	@AfterClass
	public static void teardown() {
		logger.info(LogColor.ThinnerPurple + " @AfterClass - FailedTestRunner running one time" + LogColor.RESET);
	}
}
