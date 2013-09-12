package ch.openech.mj.model.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

import ch.openech.mj.db.EmptyObjects;
import ch.openech.mj.edit.value.CloneHelper;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.util.FieldUtils;

public class FieldProperty implements PropertyInterface {
	private final Field field;
	private final boolean isFinal;
	
	public FieldProperty(Class<?> clazz, Field field) {
		this.field = field;
		this.isFinal = FieldUtils.isFinal(field);
	}

	@Override
	public Class<?> getDeclaringClass() {
		return field.getDeclaringClass();
	}

	@Override
	public Object getValue(Object object) {
		try {
			return field.get(object);
		} catch (Exception e) {
			System.out.println("E: " + e.getLocalizedMessage());
			System.out.println("O: " + object + (object != null ? "  (" + object.getClass() +")" : ""));
			System.out.println("F: " + field.getName() + " (" + field.getType() + ")");
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setValue(Object object, Object value) {
		try {
			if (!isFinal) {
				field.set(object, value);
			} else {
				Object finalObject = field.get(object);
				if (finalObject == value) return;
				if (finalObject instanceof List) {
					List<?> finalList = (List<?>) finalObject;
					finalList.clear();
					if (value != null) {
						finalList.addAll((List) value);
					}
				} else {
					if (value == null) {
						value = EmptyObjects.getEmptyObject(field.getType());
					}
					CloneHelper.deepCopy(value, finalObject);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getFieldName() {
		return field.getName();
	}

	@Override
	public String getFieldPath() {
		return getFieldName();
	}

	@Override
	public Type getType() {
		return field.getGenericType();
	}

	@Override
	public Class<?> getFieldClazz() {
		return field.getType();
	}
	
	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return field.getAnnotation(annotationClass);
	}

	@Override
	public boolean isFinal() {
		return isFinal;
	}
}