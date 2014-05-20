package org.minimalj.application;

public class DevMode {

	public static boolean isActive() {
		return System.getProperty("MjDevMode", "false").equals("true");
	}
	
}
