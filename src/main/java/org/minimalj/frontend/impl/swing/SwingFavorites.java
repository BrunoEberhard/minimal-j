package org.minimalj.frontend.impl.swing;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.minimalj.application.Application;

public class SwingFavorites {

	private final Preferences preferences;
	
	public SwingFavorites() {
		preferences = Preferences.userNodeForPackage(Application.getInstance().getClass()).node("favorites");
	}

	public LinkedHashMap<String, String> getFavorites() {
		LinkedHashMap<String, String> favorites = new LinkedHashMap<>();
		try {
			String[] keys = preferences.keys();
			Arrays.sort(keys);
			for (String key : keys) {
				try {
					String route = key.substring(key.indexOf("@")+1);
					favorites.put(route, preferences.get(key, "Page"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (BackingStoreException e) {
			// do nothing, favorites not available
		}
		return favorites;
	}
	
	public void addFavorite(String route, String title) {
		LocalDateTime time = LocalDateTime.now();
		String timeString = time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		
		preferences.put(timeString + "@" + route, title);
	}
}
