package org.minimalj.test;

import java.awt.Window;
import java.lang.reflect.Field;
import java.util.Properties;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.web.WebServer;

public class TestUtil {

	public static void shutdown() {
		try {
			Field field = Application.class.getDeclaredField("instance");
			field.setAccessible(true);
			field.set(null, null);
			
			field = Frontend.class.getDeclaredField("instance");
			field.setAccessible(true);
			field.set(null, null);
			
			field = Backend.class.getDeclaredField("instance");
			field.setAccessible(true);
			field.set(null, null);
			
			field = Configuration.class.getDeclaredField("externalProperties");
			field.setAccessible(true);
			((Properties) field.get(null)).clear();

			for (Window w : Window.getWindows()) {
				w.setVisible(false);
			}
			
			WebServer.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
