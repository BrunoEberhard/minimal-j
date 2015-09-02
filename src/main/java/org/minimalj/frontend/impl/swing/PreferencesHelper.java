package org.minimalj.frontend.impl.swing;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.minimalj.model.Code;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.Codes;
import org.minimalj.util.IdUtils;

public abstract class PreferencesHelper {
	private static final Logger logger = Logger.getLogger(PreferencesHelper.class.getName());
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void load(Preferences preferences, Object data) {
		for (Entry<String, PropertyInterface> entry : FlatProperties.getProperties(data.getClass()).entrySet()) {
			String key = entry.getKey();
			Class<?> clazz = entry.getValue().getClazz();
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
				value = LocalDate.parse(formattedDate, DateTimeFormatter.ISO_DATE);
			} else if (Enum.class.isAssignableFrom(clazz)) {
				Enum<?> presetValue = (Enum<?>) FlatProperties.getValue(data, key);
				int ordinal = preferences.getInt(key, presetValue != null ? presetValue.ordinal() : 0);
				value = EnumUtils.valueList((Class<Enum>) presetValue.getClass()).get(ordinal);
			} else if (Code.class.isAssignableFrom(clazz)) {
				Code presetValue = (Code) FlatProperties.getValue(data, key);
				PropertyInterface property = FlatProperties.getProperty(clazz, "id");
				if (property.getClazz() == String.class) {
					String id = preferences.get(key, null);
					if (id != null) {
						Class clazz2 = clazz;
						value = Codes.findCode(clazz2, id);
					} else {
						value = presetValue;
					}
				} else if (property.getClazz() == Integer.class) {
					Integer id = preferences.getInt(key, 0);
					if (id != 0) {
						Class clazz2 = clazz;
						value = Codes.findCode(clazz2, id);
					} else {
						value = presetValue;
					}
				}
			} else {
				logger.warning("Preference Field with unsupported Class: " + key + " of class " + clazz.getSimpleName() + " in " + data.getClass().getSimpleName());
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
			} else if (value instanceof Code) {
				Object id = IdUtils.getId(value);
				if (id instanceof String) {
					preferences.put(key, (String) id);
				} else if (id instanceof Integer) {
					preferences.putInt(key, (Integer) id);
				} else {
					throw new IllegalArgumentException("Only codes with String or Integer ids are allowed for preferences object");
				}
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

}
