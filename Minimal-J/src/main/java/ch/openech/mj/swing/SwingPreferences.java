package ch.openech.mj.swing;

import java.util.prefs.Preferences;

public class SwingPreferences {

	private static Preferences preferences;
	
	public static Preferences getPreferences() {
		if (preferences == null) {
			throw new IllegalStateException("SwingPreferences has to be initialized");
		}
		return preferences;
	}

	public static synchronized void setToolkit(Preferences preferences) {
		if (SwingPreferences.preferences != null) {
			throw new IllegalStateException("SwingPreferences cannot be changed");
		}		
		if (preferences == null) {
			throw new IllegalArgumentException("SwingPreferences cannot be null");
		}
		SwingPreferences.preferences = preferences;
	}
	
}
