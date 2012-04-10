package ch.openech.mj.db.model;

import java.lang.reflect.Method;
import java.util.ResourceBundle;

import ch.openech.mj.util.StringUtils;

public class InternalCode extends Code {

	public InternalCode(Class<? extends Enum<? extends CodeValue>> clazz) {
		this(clazz, ResourceBundle.getBundle(clazz.getName()));
	}
	
	public InternalCode(Class<? extends Enum<? extends CodeValue>> clazz, ResourceBundle resourceBundle) {
		super(resourceBundle);
		readValues(clazz);
	}
	
	@Override
	protected void readValues() {
		// override and use own method in constructor
	}

	private void readValues(Class<?> clazz) {
		try {
			Method valuesMethod = clazz.getMethod("values");
			CodeValue[] values = (CodeValue[]) valuesMethod.invoke(null);
 			for (CodeValue value : values) {
 				String key = value.getKey();
 				String text = getString("text." + key);
 				if (StringUtils.isBlank(text)) {
 					text = "Wert " + key;
 				}
 				items.add(new CodeItem(key, text.trim()));
 			}
		} catch (Exception x) {
			new RuntimeException(x);
		}
	}
	
	@Override
	public Class<String> getClazz() {
		return String.class; // !!
	}

}

/* Generic variante
public class InternalCode<T extends Enum<? extends CodeValue>> extends Code {

	private Class<T> clazz;

	public InternalCode(Class<T> clazz) {
		this(clazz, ResourceBundle.getBundle(clazz.getName()));
	}
	
	public InternalCode(Class<T> clazz, ResourceBundle resourceBundle) {
		super(resourceBundle);
		this.clazz = clazz;
		readValues(clazz);
	}
	
	@Override
	protected void readValues() {
		// override and use own method in constructor
	}

	private void readValues(Class<T> clazz) {
		try {
			Method valuesMethod = clazz.getMethod("values");
			CodeValue[] values = (CodeValue[]) valuesMethod.invoke(null);
 			for (CodeValue value : values) {
 				String key = value.getKey();
 				keys.add(value.getKey());
 				String text = getString("text." + key);
 				if (StringUtils.isBlank(text)) text = "Wert " + key;
 				texts.add(text.trim());
 			}
		} catch (Exception x) {
			new RuntimeException(x);
		}
	}
	
	@Override
	public Class<String> getClazz() {
		return String.class; // !!
	}

}
*/