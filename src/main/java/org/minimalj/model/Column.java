package org.minimalj.model;

import java.lang.annotation.Annotation;

import org.minimalj.frontend.impl.util.ColumnFilter;
import org.minimalj.model.Rendering.ColorName;
import org.minimalj.model.properties.PropertyInterface;

public abstract class Column<ROW, COLUMN> implements PropertyInterface {

	protected final PropertyInterface property;

	public Column(Object key) {
		this.property = Keys.getProperty(key);
	}

	public CharSequence render(ROW rowObject, COLUMN value) {
		return Rendering.render(value);
	}
	
	public ColorName getColor(ROW rowObject, COLUMN value) {
		return null;
	}

	public Runnable getRunnable(ROW rowObject, COLUMN value) {
		return null;
	}
	
	public ColumnFilter getFilter() {
		return null;
	}
	
	// delegation
	
	public final Class<?> getDeclaringClass() {
		return property.getDeclaringClass();
	}

	public final String getName() {
		return property.getName();
	}

	public final String getPath() {
		return property.getPath();
	}

	@SuppressWarnings("unchecked")
	public final Class<ROW> getClazz() {
		return (Class<ROW>) property.getClazz();
	}

	public final Class<?> getGenericClass() {
		return property.getGenericClass();
	}

	public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return property.getAnnotation(annotationClass);
	}

	@SuppressWarnings("unchecked")
	public final COLUMN getValue(Object object) {
		return (COLUMN) property.getValue(object);
	}

	public final void setValue(Object object, Object value) {
		property.setValue(object, value);
	}

	public final boolean isFinal() {
		return property.isFinal();
	}
}
