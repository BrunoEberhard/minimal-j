package ch.openech.mj.swing;

import java.util.logging.Logger;
import java.util.prefs.Preferences;

import ch.openech.mj.db.model.ColumnAccess;

public abstract class PreferencesHelper {
	private static final Logger logger = Logger.getLogger(PreferencesHelper.class.getName());
	
	private static ThreadLocal<Preferences> preferences = new ThreadLocal<Preferences>();

	public static void setPreferences(Preferences preferences) {
		if (preferences == null) {
			throw new IllegalArgumentException("Preferences instance cannot be null");
		}
		PreferencesHelper.preferences.set(preferences);
	}
	
	public static Preferences preferences() {
		return PreferencesHelper.preferences.get();
	}
	
	public static void load(Object data) {
		for (String key : ColumnAccess.getKeys(data.getClass())) {
			Class<?> clazz = ColumnAccess.getType(data.getClass(), key);
			Object value = null;
			if (String.class.equals(clazz)) {
				String presetValue = (String) ColumnAccess.getValue(data, key);
				value = preferences().get(key, presetValue);
			} else if (Integer.class.equals(clazz) || Integer.TYPE.equals(clazz)) {
				Integer presetValue = (Integer) ColumnAccess.getValue(data, key);
				value = preferences().getInt(key, presetValue != null ? presetValue.intValue() : 0);
			} else if (Boolean.class.equals(clazz) || Boolean.TYPE.equals(clazz)) {
				Boolean presetValue = (Boolean) ColumnAccess.getValue(data, key);
				value = preferences().getBoolean(key, presetValue != null ? presetValue.booleanValue() : false);
			} else {
				logger.warning("Preference Field with unsupported Class: " + key + " of class " + clazz.getName());
			}
			ColumnAccess.setValue(data, key, value);
		}
	}

	public static void save(Object object) {
		for (String key : ColumnAccess.getKeys(object.getClass())) {
			Object value = ColumnAccess.getValue(object, key);
			if (value == null) {
				preferences().remove(key);
			} else if (value instanceof Boolean) {
				preferences().putBoolean(key, (Boolean)value);
			} else if (value instanceof String) {
				preferences().put(key, (String)value);
			} else if (value instanceof Integer) {
				preferences().putInt(key, (Integer)value);
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

}
