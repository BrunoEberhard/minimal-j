package org.minimalj.frontend.swing;

import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.joda.time.LocalDate;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.PropertyInterface;
import org.minimalj.model.properties.FlatProperties;

public abstract class PreferencesHelper {
	private static final Logger logger = Logger.getLogger(PreferencesHelper.class.getName());
	
	public static void load(Preferences preferences, Object data) {
		for (Entry<String, PropertyInterface> entry : FlatProperties.getProperties(data.getClass()).entrySet()) {
			String key = entry.getKey();
			Class<?> clazz = entry.getValue().getFieldClazz();
			Object value = null;
			if (String.class.equals(clazz)) {
				String presetValue = (String) FlatProperties.getValue(data, key);
				value = preferences.get(key, presetValue);
			} else if (Integer.class.equals(clazz) || Integer.TYPE.equals(clazz)) {
				Integer presetValue = (Integer) FlatProperties.getValue(data, key);
				value = preferences.getInt(key, presetValue != null ? presetValue.intValue() : 0);
			} else if (Boolean.class.equals(clazz) || Boolean.TYPE.equals(clazz)) {
				Boolean presetValue = (Boolean) FlatProperties.getValue(data, key);
				value = preferences.getBoolean(key, presetValue != null ? presetValue.booleanValue() : false);
			} else if (clazz == LocalDate.class) {
				LocalDate presetValue = (LocalDate) FlatProperties.getValue(data, key);
				String formattedDate = preferences.get(key, presetValue != null ? presetValue.toString() : null);
				value = new LocalDate(formattedDate);
			} else if (Enum.class.isAssignableFrom(clazz)) {
				Enum<?> presetValue = (Enum<?>) FlatProperties.getValue(data, key);
				int ordinal = preferences.getInt(key, presetValue != null ? presetValue.ordinal() : 0);
				value = EnumUtils.valueList((Class<Enum>) presetValue.getClass()).get(ordinal);
			} else {
				logger.warning("Preference Field with unsupported Class: " + key + " of class " + clazz.getName());
			}
			FlatProperties.set(data, key, value);
		}
	}

	public static void save(Preferences preferences, Object object) {
		for (Entry<String, PropertyInterface> entry : FlatProperties.getProperties(object.getClass()).entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue().getValue(object);
			if (value == null) {
				preferences.remove(key);
			} else if (value instanceof Boolean) {
				preferences.putBoolean(key, (Boolean)value);
			} else if (value instanceof String) {
				preferences.put(key, (String)value);
			} else if (value instanceof Integer) {
				preferences.putInt(key, (Integer)value);
			} else if (value instanceof Enum<?>) {
				Enum<?> e = (Enum<?>) value;
				preferences.putInt(key, e.ordinal());
			} else if (value instanceof LocalDate) {
				preferences.put(key, ((LocalDate) value).toString());
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

}
