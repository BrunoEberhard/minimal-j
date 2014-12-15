package org.minimalj.frontend.edit.fields;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import org.minimalj.frontend.edit.form.Form;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.DemoEnabled;
import org.minimalj.util.GenericUtils;

// TODO: Typisierung bringt hier so was von nichts
public class EnumSetEditField<E extends Set<Enum<?>>> extends ObjectFlowField<E> implements Enable, DemoEnabled {
	private final Class enumClass;
	private final Collection allowedValues;
	
	public EnumSetEditField(PropertyInterface property, boolean editable) {
		this(property, null, editable);
	}

	public EnumSetEditField(E key, E allowedValues) {
		this(Keys.getProperty(key), allowedValues, true);
	}
		
	public EnumSetEditField(PropertyInterface property, E allowedValues, boolean editable) {
		super(property, editable);
		this.enumClass = (Class) GenericUtils.getGenericClass(property.getType());
		this.allowedValues = allowedValues != null ? allowedValues : EnumUtils.valueList(enumClass);
	}
	
	@Override
	public void fillWithDemoData() {
		E newValues = (E) new HashSet();
		for (Object object : allowedValues) {
			if (Math.random() <0.5) {
				Enum<?> value = (Enum<?>) object;
				newValues.add(value);
			}
		}
		setObject(newValues);
	}

	@Override
	protected Form<E> createFormPanel() {
		String bundleName = enumClass.getName();
		ResourceBundle resourceBundle = ResourceBundle.getBundle(bundleName);
		Form<E> form = new Form<E>(resourceBundle, true);
		for (Object object : allowedValues) {
			Enum<?> value = (Enum<?>) object;
			form.line(new CheckBoxField(new EnumSetFieldEditorPropertyInterface(value), EnumUtils.getText((Enum) object), true));
		}
		return form;
	}

	@Override
	protected void show(E objects) {
		for (Object object : objects) {
			addObject(object);
		}
	}

	@Override
	protected void showActions() {
		addAction(new ObjectFieldEditor());
	}
	
	private class EnumSetFieldEditorPropertyInterface implements PropertyInterface {
		private final Enum<?> value;

		public EnumSetFieldEditorPropertyInterface(Enum<?> value) {
			this.value = value;
		}

		@Override
		public Class<?> getDeclaringClass() {
			return enumClass;
		}

		@Override
		public String getName() {
			return value.name();
		}

		@Override
		public String getPath() {
			return value.name();
		}

		@Override
		public Class<?> getClazz() {
			return enumClass;
		}

		@Override
		public Type getType() {
			return null;
		}

		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return null;
		}

		@Override
		public Object getValue(Object object) {
			Set set = (Set) object;
			return set.contains(value);
		}

		@Override
		public void setValue(Object object, Object newValue) {
			Set set = (Set) object;
			if (Boolean.TRUE.equals(newValue) == true) {
				set.add(value);
			} else {
				set.remove(value);
			}
		}

		@Override
		public boolean isFinal() {
			return false;
		}
	}

}
