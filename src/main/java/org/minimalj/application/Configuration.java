package org.minimalj.application;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.util.LoggingRuntimeException;

/**
 * Properties are considered in the following order:
 * 
 * <OL>
 * <LI>Properties set from outside, for example servlets init parameters</LI>
 * <LI>System properties</LI>
 * <LI>Properties loaded with the config file specified with <code>MjConfig</code></LI>
 * </OL>
 *
 */
public class Configuration {
	private static final Logger logger = Logger.getLogger(Configuration.class.getName());
	private static final Properties configFileProperties = new Properties();
	private static final Properties externalProperties = new Properties();
	
	static {
		String configFileName = System.getProperty("MjConfig");
		if (configFileName != null) {
			try (FileInputStream fis = new FileInputStream(configFileName)) {
				configFileProperties.load(fis);
			} catch (FileNotFoundException e) {
				logger.log(Level.SEVERE, "Config file not found: " + configFileName, e);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Unable to read Config file: " + configFileName, e);
			}
		}
	}
	
	public static void set(String key, String value) {
		externalProperties.setProperty(key, value);
	}
	
	public static String get(String key, String defaultValue) {
		if (externalProperties.containsKey(key)) {
			return externalProperties.getProperty(key);
		} else if (System.getProperties().containsKey(key)) {
			return System.getProperty(key);
		} else if (configFileProperties.containsKey(key)) {
			return configFileProperties.getProperty(key);
		} else {
			return defaultValue;
		}
	}
	
	public static String get(String key) {
		return get(key, null);
	}
	
	public static boolean available(String key) {
		return get(key) != null;
	}
	
	public static <T> T getClazz(String key, Class<T> clazz, Object... parameters) {
		String className = get(key);
		try {
			@SuppressWarnings("unchecked")
			Class<? extends T> configuredClass = (Class<? extends T>) Class.forName(className);
			if (parameters == null) {
				return configuredClass.newInstance();
			} else {
				Class<?>[] parameterTypes = new Class[parameters.length];
				for (int i = 0; i<parameters.length; i++) {
					parameterTypes[i] = parameters[i].getClass();
				}
				Constructor<? extends T> constructor = configuredClass.getConstructor(parameterTypes);
				return constructor.newInstance(parameters);
			}
		} catch (ClassNotFoundException e) {
			throw new LoggingRuntimeException(e, logger, className + " not found / " + key + " configuration failed");
		} catch (ClassCastException cce) {
			throw new LoggingRuntimeException(cce, logger, className + " is not a " + clazz.getName() + ". " + key + " configuration failed");
		} catch (Exception x) {
			throw new LoggingRuntimeException(x, logger, className + " / " + key + " configuration failed");
		}
	}
}