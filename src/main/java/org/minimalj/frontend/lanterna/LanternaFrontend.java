package org.minimalj.frontend.lanterna;

import java.util.prefs.Preferences;

import org.minimalj.application.Application;
import org.minimalj.application.ApplicationContext;
import org.minimalj.frontend.lanterna.component.HighContrastLanternaTheme;
import org.minimalj.frontend.lanterna.toolkit.LanternaClientToolkit;
import org.minimalj.frontend.swing.PreferencesHelper;
import org.minimalj.frontend.toolkit.ClientToolkit;

import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.swing.SwingTerminal;

public class LanternaFrontend {

	private static ApplicationContext applicationContext;

	private LanternaFrontend() {
		// private
	}
	
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void run() {
		try {
			ClientToolkit.setToolkit(new LanternaClientToolkit());

			SwingTerminal terminal = new SwingTerminal();
			Screen screen = new Screen(terminal);

			LanternaGUIScreen gui = new LanternaGUIScreen(screen);
			gui.setTheme(new HighContrastLanternaTheme());

			applicationContext = new LanternaApplicationContext();

			screen.startScreen();
			gui.init();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	public class LanternaApplicationContext extends ApplicationContext {
		private String user;

		public LanternaApplicationContext() {
		}

		@Override
		public void setUser(String user) {
			this.user = user;
		}

		@Override
		public String getUser() {
			return user;
		}

		@Override
		public void savePreferences(Object preferences) {
			PreferencesHelper.save(Preferences
					.userNodeForPackage(LanternaFrontend.this.getClass()),
					preferences);
		}

		@Override
		public void loadPreferences(Object preferences) {
			PreferencesHelper.load(Preferences
					.userNodeForPackage(LanternaFrontend.this.getClass()),
					preferences);
		}
	}

	public static void main(final String[] args) throws Exception {
		Application.initApplication(args);

		new LanternaFrontend().run();
	}
}
