package org.minimalj.frontend.impl.util;

import java.util.LinkedHashMap;

public interface FavoritesManager {

	void setUser(String user);

	LinkedHashMap<String, String> getFavorites();

	boolean isFavorite(String route);

	void toggleFavorite(String route, String title);

}