package org.minimalj.frontend.impl.swing;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import org.minimalj.application.Application;

// for: java.util.prefs Could not open/create prefs root node Software\JavaSoft\Prefs
// see: https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8139507
public class SwingFavorites implements PreferenceChangeListener {

	private final Consumer<LinkedHashMap<String, String>> changeListener;
	private Preferences preferences;

	public SwingFavorites(Consumer<LinkedHashMap<String, String>> changeListener) {
		Objects.nonNull(changeListener);
		
		setUser(null);

		this.changeListener = changeListener;
	}

	public void setUser(String user) {
		if (preferences != null) {
			preferences.removePreferenceChangeListener(this);
		}

		if (user != null) {
			preferences = Preferences.userNodeForPackage(Application.getInstance().getClass()).node("favoritesByUser").node(user);
		} else {
			preferences = Preferences.userNodeForPackage(Application.getInstance().getClass()).node("favorites");
		}			

		preferences.addPreferenceChangeListener(this);
		
		if (changeListener != null) {
			changeListener.accept(getFavorites());
		}
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent evt) {
		changeListener.accept(getFavorites());
	}

	public LinkedHashMap<String, String> getFavorites() {
		try {
			String[] keys = preferences.keys();
			Arrays.sort(keys);
			LinkedHashMap<String, String> favorites = new LinkedHashMap<>();
			for (String key : keys) {
				try {
					String route = key.substring(key.indexOf("@") + 1);
					favorites.put(route, preferences.get(key, "Page"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return favorites;
		} catch (BackingStoreException e) {
			// do nothing, favorites not available
			return new LinkedHashMap<>();
		}
	}

	public boolean isFavorite(String route) {
		return findKey(route) != null;
	}

	private String findKey(String route) {
		route = "@" + route;
		String[] keys;
		try {
			keys = preferences.keys();
			for (String key : keys) {
				if (key.endsWith(route)) {
					return key;
				}
			}
		} catch (BackingStoreException e) {
			// do nothing, favorites not available
		}
		return null;
	}

	private void addFavorite(String route, String title) {
		LocalDateTime time = LocalDateTime.now();
		String timeString = time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

		String key = timeString + "@" + route;
		preferences.put(key, title);
	}

	private void removeFavorite(String route) {
		route = "@" + route;
		String[] keys;
		try {
			keys = preferences.keys();
			for (String key : keys) {
				if (key.endsWith(route)) {
					preferences.remove(key);
				}
			}
		} catch (BackingStoreException e) {
			// do nothing, favorites not available
		}
	}

	public void toggleFavorite(String route, String title) {
		String key = findKey(route);
		if (key != null) {
			removeFavorite(route);
		} else {
			addFavorite(route, title);
		}
	}

}
