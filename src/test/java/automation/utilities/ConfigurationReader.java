package automation.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Reads key/value pairs from the active properties file.
 *
 * By default {@code Configuration.properties} is loaded. Pass {@code -Dprofile=qa} to load {@code qa.properties}
 * instead. Any key can also be overridden from the command line, e.g. {@code -Durl=https://...}.
 */
public class ConfigurationReader {

	private static final Properties configFile = new Properties();
	public static final Logger logger = LogManager.getLogger(ConfigurationReader.class);

	static {
		String profile = System.getProperty("profile");
		String path = (profile == null || profile.isBlank()) ? "Configuration.properties" : profile + ".properties";
		logger.info(LogColor.Magenta + "****************** Current Config File: " + path + " ******************"
				+ LogColor.RESET);

		try (FileInputStream input = new FileInputStream(path)) {
			configFile.load(input);
		} catch (IOException e) {
			throw new IllegalStateException("Unable to load configuration file: " + path, e);
		}
	}

	private ConfigurationReader() {
	}

	public static String getProperty(String keyName) {
		String override = System.getProperty(keyName);
		if (override != null && !override.isBlank()) {
			return override;
		}
		return configFile.getProperty(keyName);
	}

	public static String getProperty(String keyName, String defaultValue) {
		String value = getProperty(keyName);
		return (value == null || value.isBlank()) ? defaultValue : value;
	}

	public static boolean getBoolean(String keyName) {
		return Boolean.parseBoolean(getProperty(keyName, "false"));
	}

	public static int getInt(String keyName, int defaultValue) {
		return Integer.parseInt(getProperty(keyName, String.valueOf(defaultValue)));
	}
}
