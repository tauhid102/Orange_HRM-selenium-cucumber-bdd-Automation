package automation.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import com.aventstack.extentreports.markuputils.MarkupHelper;

/** Takes screenshots and writes log entries into the step of the Extent report that is currently running. */
public class ScreenshotUtil {

	private static final Logger logger = LogManager.getLogger(ScreenshotUtil.class);

	private ScreenshotUtil() {
	}

	public static byte[] takeScreenshot() {
		if (!Driver.hasDriver()) {
			return null;
		}
		try {
			return ((TakesScreenshot) Driver.getDriver()).getScreenshotAs(OutputType.BYTES);
		} catch (Exception e) {
			logger.warn("Unable to take screenshot: " + e.getMessage());
			return null;
		}
	}

	public static void logInfo(String message) {
		ExtentTest step = currentStep();
		if (step != null) {
			step.info(message);
		}
	}

	public static void logCodeBlock(String title, String content) {
		ExtentTest step = currentStep();
		if (step != null) {
			step.info(title);
			step.info(MarkupHelper.createCodeBlock(content));
		}
	}

	/** Logs a failure in the report, with a screenshot attached when a browser is open. */
	public static void logFailure(String message) {
		ExtentTest step = currentStep();
		if (step == null) {
			return;
		}
		String base64 = Driver.hasDriver() ? takeBase64Screenshot() : null;
		if (base64 != null) {
			step.fail(message, MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
		} else {
			step.fail(message);
		}
	}

	private static String takeBase64Screenshot() {
		try {
			return ((TakesScreenshot) Driver.getDriver()).getScreenshotAs(OutputType.BASE64);
		} catch (Exception e) {
			logger.warn("Unable to take screenshot: " + e.getMessage());
			return null;
		}
	}

	private static ExtentTest currentStep() {
		try {
			return ExtentCucumberAdapter.getCurrentStep();
		} catch (Exception e) {
			return null;
		}
	}
}
