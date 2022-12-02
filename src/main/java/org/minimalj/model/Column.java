package org.minimalj.model;

import java.lang.annotation.Annotation;
import java.util.Objects;

import org.minimalj.frontend.impl.util.ColumnFilter;
import org.minimalj.model.Rendering.ColorName;
import org.minimalj.model.properties.Property;
import org.minimalj.util.resources.Resources;

public abstract class Column<ROW, COLUMN> implements Property {

	protected final Property property;
	
	/**
	 * start is <code>null</code>
	 */
	public enum ColumnAlignment { center, end };
	
	public Column(Object key) {
		this.property = Objects.requireNonNull(Keys.getProperty(key));
	}
	
	public CharSequence render(ROW rowObject, COLUMN value) {
		return Rendering.render(value);
	}
	
	public ColorName getColor(ROW rowObject, COLUMN value) {
		return null;
	}

	public boolean isLink(ROW rowObject, COLUMN value) {
		return false;
	}
	
	public void run(ROW rowObject) {
		//
	}
	
	public ColumnFilter getFilter() {
		return null;
	}
	
	public Integer getWidth() {
		return null;
	}
	
	public Integer getMaxWidth() {
		return null;
	}

	public ColumnAlignment getAlignment() {
		return null;
	}
	
	public String getHeader() {
		return Resources.getPropertyName(property);
	}
	
	public static String evalHeader(Property property) {
		if (property instanceof Column) {
			return ((Column<?, ?>) property).getHeader();
		} else {
			return Resources.getPropertyName(property);
		}
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
