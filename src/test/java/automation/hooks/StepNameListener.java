package automation.hooks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import automation.utilities.LogColor;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestStepStarted;

/** Cucumber plugin that prints each Gherkin step to the log before it runs. */
public class StepNameListener implements ConcurrentEventListener {

	public static final Logger logger = LogManager.getLogger(StepNameListener.class);

	private final EventHandler<TestStepStarted> stepHandler = event -> {
		if (event.getTestStep() instanceof PickleStepTestStep) {
			PickleStepTestStep testStep = (PickleStepTestStep) event.getTestStep();
			printStepName(testStep.getStep().getKeyword() + testStep.getStep().getText());
		}
	};

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(TestStepStarted.class, stepHandler);
	}

	private void printStepName(String currentStepName) {
		logger.info(LogColor.DarkOrange + "================================================================" + LogColor.RESET);
		logger.info(LogColor.DarkOrange + "Executing step: " + currentStepName + LogColor.RESET);
		logger.info(LogColor.DarkOrange + "================================================================" + LogColor.RESET);
	}
}
