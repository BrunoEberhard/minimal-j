package ch.openech.mj.db.model;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import ch.openech.mj.db.model.annotation.Is;
import ch.openech.mj.edit.value.PropertyAccessor;
import ch.openech.mj.util.StringUtils;

public class Formats {

	private static Formats instance = new Formats();
	
	private Map<String, Format> formats = new HashMap<String, Format>();
	
	private Formats() {
		// nothing to do
	}
	
	public static Formats getInstance() {
		return instance;
	}
	
	//

	public void register(String name, Format format) {
		formats.put(name, format);
	}
	
	public void registerCode(String name, ResourceBundle resourceBundle) {
		registerCode(name, resourceBundle, name);
	}

	public void registerCode(String name, ResourceBundle resourceBundle, String prefix) {
		formats.put(name, new Code(resourceBundle, prefix));
	}

	/**
	 * Registers an internal code (based on a enumeration class) under the name
	 * of the clazz
	 * 
	 * @param clazz
	 */
	public <T extends Enum<? extends CodeValue>> void registerCode(Class<T> clazz) {
		String name = (StringUtils.lowerFirstChar(clazz.getSimpleName()));
		registerCode(name, clazz);
	}

	public <T extends Enum<? extends CodeValue>> void registerCode(String name, Class<T> clazz) {
		formats.put(name, new InternalCode(clazz));
	}

	public Format getFormat(Class<?> clazz, Object key) {
		AccessorInterface accessor = PropertyAccessor.getAccessor(clazz, Constants.getConstant(key));
		return getFormat(accessor);
	}

	public Format getFormat(AccessorInterface accessor) {
		String formatName = getFormatName(accessor);
		return getFormat(formatName);
	}

	private Format getFormat(String name) {
		return formats.get(name);
	}

	private String getFormatName(AccessorInterface accessor) {
		Is type = accessor.getAnnotation(Is.class);
		if (type != null) {
			return type.value();
		} else {
			return accessor.getName();
		}
	}
}
