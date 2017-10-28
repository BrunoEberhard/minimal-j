package org.minimalj.frontend.impl.swing;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.minimalj.application.Application;

public class SwingFavorites {

	private static final Preferences preferences;
	private static LinkedHashMap<String, String> favorites = new LinkedHashMap<>();
	
	static {
		preferences = Preferences.userNodeForPackage(Application.getInstance().getClass()).node("favorites");
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
	}

	public static LinkedHashMap<String, String> getFavorites() {
		return favorites;
	}
	
	public static boolean isFavorite(String route) {
		for (String key : favorites.keySet()) {
			if (key.equals(route)) {
				return true;
			}
		}
		return false;
	}
	
	public static void addFavorite(String route, String title) {
		LocalDateTime time = LocalDateTime.now();
		String timeString = time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		
		preferences.put(timeString + "@" + route, title);
	}

	public static void toggleFavorite(String route, String title) {
		String toRemove = null;
		for (String key : favorites.keySet()) {
			if (key.equals(route)) {
				toRemove = key;
				break;
			}
		}
		if (toRemove != null) {
			favorites.remove(toRemove);
		} else {
			addFavorite(route, title);
		}
	}
}
