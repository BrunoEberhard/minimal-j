package org.minimalj.application;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Configuration {
	private static final Logger logger = Logger.getLogger(Configuration.class.getName());
	private static final Properties config = new Properties();
	
	static {
		String configFileName = System.getProperty("config");
		if (configFileName != null) {
			try (FileInputStream fis = new FileInputStream(configFileName)) {
				config.load(fis);
			} catch (FileNotFoundException e) {
				logger.log(Level.SEVERE, "Config file not found: " + configFileName, e);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Unable to read Config file: " + configFileName, e);
			}
		}
	}
	
	public static void set(String key, String value) {
		config.setProperty(key, value);
	}
	
	public static String get(String key, String defaultValue) {
		if (config.containsKey(key)) {
			return config.getProperty(key);
		} else {
			return System.getProperty(key, defaultValue);
		}
	}
	
	public static String get(String key) {
		return get(key, null);
	}
}
