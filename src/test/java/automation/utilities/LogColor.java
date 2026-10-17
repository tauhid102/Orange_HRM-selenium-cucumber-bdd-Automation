package automation.utilities;

/** ANSI colour codes used to make console logs easier to scan. */
public class LogColor {

	public final static String RED = "\u001B[31;1m";
	public final static String Blue = "\u001B[34;1m";
	public final static String Purple = "\u001B[1;35m";
	public final static String ThinnerPurple = "\u001B[38;5;135m";
	public final static String DarkGreen = "\u001B[32;1m";
	public final static String DarkOrange = "\u001B[38;5;208m";
	public final static String Cyan = "\u001B[38;5;51m";
	public final static String Magenta = "\u001B[38;5;201m";
	public final static String Indigo = "\u001B[38;5;54m";
	public final static String RESET = "\u001B[0m";

	private LogColor() {
	}
}
