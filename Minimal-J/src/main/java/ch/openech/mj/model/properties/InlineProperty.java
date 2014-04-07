package ch.openech.mj.model.properties;

import java.lang.reflect.Field;

import ch.openech.mj.edit.value.CloneHelper;

public class InlineProperty extends SimpleProperty {

	public InlineProperty(Class<?> clazz, Field field) {
		super(clazz, field);
	}

	@Override
	public void setValue(Object object, Object value) {
		Object finalValue = getValue(object);
		CloneHelper.deepCopy(value, finalValue);
	}
}