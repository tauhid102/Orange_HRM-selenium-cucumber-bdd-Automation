package automation.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Soft assertion that keeps the scenario running after a failed check. Every check is written to the log and the
 * Extent report (failures get a screenshot). {@link #assertAll()} is called from the @After hook and fails the
 * scenario if any check failed.
 */
public class SftAssert {

	private static final Logger logger = LogManager.getLogger(SftAssert.class);
	private final List<String> failureMessages = new ArrayList<>();

	public void softAssertTrue(boolean condition, String passMessage, String failMessage) {
		if (condition) {
			handlePass(passMessage);
		} else {
			handleFail(failMessage);
		}
	}

	public void softAssertFalse(boolean condition, String passMessage, String failMessage) {
		softAssertTrue(!condition, passMessage, failMessage);
	}

	public void softAssertEquals(Object actual, Object expected, String message) {
		if (Objects.equals(actual, expected)) {
			handlePass(message + " [Value: " + actual + "]");
		} else {
			handleFail(message + " [Expected: " + expected + ", Actual: " + actual + "]");
		}
	}

	public void softAssertFail(String message) {
		handleFail("Forced Failure: " + message);
	}

	public int getFailureCount() {
		return failureMessages.size();
	}

	public void assertAll() {
		if (failureMessages.isEmpty()) {
			return;
		}
		StringBuilder summary = new StringBuilder("The following " + failureMessages.size() + " check(s) failed:");
		for (String msg : failureMessages) {
			summary.append(System.lineSeparator()).append(" - ").append(msg);
		}
		failureMessages.clear();
		throw new AssertionError(summary.toString());
	}

	// --- Helper Methods ---

	private void handlePass(String message) {
		logger.info(LogColor.Blue + "Assert Passed :---------------------- " + message + LogColor.RESET);
		ScreenshotUtil.logInfo("Passed: " + message);
	}

	private void handleFail(String message) {
		failureMessages.add(message);
		logger.error(LogColor.RED + "Assert Failed :------------------------ " + message + LogColor.RESET);
		ScreenshotUtil.logFailure(message);
	}
}
