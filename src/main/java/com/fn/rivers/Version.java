package com.fn.rivers;

/**
 * 
 * @author chenwen
 *
 */
public class Version {
	static String VERSION = "0.01";

	public static String getVersion() {
		return VERSION;
	}

	/**
	 * Prints the current version to the standard out.
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.exit(0);
		}
		if (args[0].equals("--version")) {
			System.out.println(getVersion());
		}
	}
}
