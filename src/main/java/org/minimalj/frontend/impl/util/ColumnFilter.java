package org.minimalj.frontend.impl.util;

import java.time.temporal.Temporal;
import java.util.function.Predicate;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.TableFilter;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.repository.query.Criteria;

public interface ColumnFilter extends Predicate<Object> {

	public PropertyInterface getProperty();

	public IComponent getComponent();

	public void setEnabled(boolean enabled);
	
	public boolean active();
	
	public ValidationMessage validate();

	public Criteria getCriteria();
	
	public static ColumnFilter createFilter(PropertyInterface property, InputComponentListener listener) {
		Class<?> clazz = property.getClazz();
		TableFilter tableFilter = property.getAnnotation(TableFilter.class);
		if (tableFilter != null) {
			// TODO
		}
		if (Temporal.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz)) {
			return new ValueOrRangeColumnFilter(property, listener);
		} else {
			return new StringColumnFilter(property, listener);
		}
	}

}
