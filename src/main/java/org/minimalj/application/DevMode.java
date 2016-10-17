package org.minimalj.application;

public class DevMode {

	public static boolean isActive() {
		return Configuration.get("MjDevMode", "false").equals("true");
	}
	
}
