package org.minimalj.frontend.impl.util;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.Validation;
import org.minimalj.util.ChangeListener;

public interface ColumnFilter extends Predicate<Object>, Validation {

	public enum ColumnFilterOperator {
		EQUALS, MIN, MAX, RANGE;
	}

	public PropertyInterface getProperty();

	public void runEditor(Consumer<String> finishedListener);

	public void setText(String text);

	public String getText();

	public boolean active();

	public boolean hasLookup();

	public static ColumnFilter createFilter(PropertyInterface property, ChangeListener<ColumnFilter> changeListener) {
		Class<?> clazz = property.getClazz();
		if (GeneralColumnFilter.isAvailableFor(clazz)) {
			return new GeneralColumnFilter(property, changeListener);
		} else {
			return new StringColumnFilter(property, changeListener);
		}
	}

}
