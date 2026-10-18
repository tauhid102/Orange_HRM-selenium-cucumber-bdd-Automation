package automation.utilities;

import net.datafaker.Faker;

/** Random but realistic test data, so tests do not depend on hard-coded names. */
public class DataGenerator {

	private static final Faker faker = new Faker();

	private DataGenerator() {
	}

	public static String firstName() {
		return faker.name().firstName();
	}

	public static String lastName() {
		return faker.name().lastName();
	}

	public static String postalCode() {
		return faker.address().zipCode();
	}

	/** Unique username (OrangeHRM needs 5 to 40 characters). */
	public static String username() {
		return "user" + System.currentTimeMillis();
	}

	/** Meets the OrangeHRM rules: at least 7 characters with a letter and a number. */
	public static String password() {
		return "Pass" + faker.number().digits(6);
	}
}
