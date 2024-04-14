package org.minimalj.example.openapiclient.page;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjModel;
import org.minimalj.metamodel.model.MjProperty;
import org.minimalj.model.Model;
import org.minimalj.model.properties.Properties;

public class PropertiesResourceBundle extends ResourceBundle {

	private final Map<String, String> resources = new HashMap<>();

	public PropertiesResourceBundle(Model model) {
		for (Class<?> clazz : model.getEntityClasses()) {
			for (String property : Properties.getProperties(clazz).keySet()) {
				String key = clazz.getSimpleName() + "." + property;
				String name = convert(property);
				resources.put(key, name);
			}
		}
	}

	public PropertiesResourceBundle(MjModel model) {
		for (MjEntity entity : model.entities) {
			for (MjProperty property : entity.properties) {
				String key = entity.getClazz().getSimpleName() + "." + property.name;
				String name = convert(property.name);
				resources.put(key, name);
			}
		}
	}

	private String convert(String key) {
		String result = "";
		for (char c : key.toCharArray()) {
			if (result.length() == 0) {
				result = String.valueOf(Character.toUpperCase(c));
			} else if (Character.isUpperCase(c)) {
				result += " " + c;
			} else {
				result += c;
			}
		}
		result = result.replaceAll(" Of", " of");
		result = result.replaceAll(" From", " from");
		result = result.replaceAll(" To", " to");
		result = result.replaceAll(" Valid", " valid");
		return result;
	}

	@Override
	protected Object handleGetObject(String key) {
		return resources.get(key);
	}

	@Override
	public Enumeration<String> getKeys() {
		List<String> keyList = new ArrayList<>(resources.keySet());
		return new Enumeration<String>() {
			private int i = 0;

			@Override
			public boolean hasMoreElements() {
				return i < keyList.size();
			}

			@Override
			public String nextElement() {
				return keyList.get(i++);
			}
		};
	}

//	@Override
//	public Enumeration<String> getKeys() {
//		return new Enumeration<String>() {
//			@Override
//			public boolean hasMoreElements() {
//				return false;
//			}
//
//			@Override
//			public String nextElement() {
//				return null;
//			}
//		};
//	}
}
