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
}
