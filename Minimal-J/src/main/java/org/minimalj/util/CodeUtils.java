package org.minimalj.util;

import java.util.List;

import org.minimalj.model.PropertyInterface;
import org.minimalj.model.annotation.Code;
import org.minimalj.model.properties.Properties;

public class CodeUtils {

	public static PropertyInterface getCodeProperty(Class<?> clazz) {
		for (PropertyInterface property : Properties.getProperties(clazz).values()) {
			if (property.getAnnotation(Code.class) != null) {
				return property;
			}
		}
		throw new IllegalArgumentException(clazz.getName() + " has no code field");
	}
	
	public static Object getCode(Object object) {
		PropertyInterface property = getCodeProperty(object.getClass());
		return property.getValue(object);
	}

	@SuppressWarnings("rawtypes")
	public static Object findCode(List codes, Object value) {
		if (value != null) {
			for (Object code : codes) {
				Object codeValue = CodeUtils.getCode(code);
				if (value.equals(codeValue)) {
					return code;
				}
			}
		} else {
			for (Object code : codes) {
				Object codeValue = CodeUtils.getCode(code);
				if (codeValue == null) {
					return code;
				}
			}
		}
		return null;
	}
	
	public static boolean isCode(Class<?> clazz) {
		for (PropertyInterface property : Properties.getProperties(clazz).values()) {
			if (property.getAnnotation(Code.class) != null) {
				return true;
			}
		}
		return false;
	}
}
