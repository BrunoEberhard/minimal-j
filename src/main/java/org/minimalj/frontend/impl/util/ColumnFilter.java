package org.minimalj.frontend.impl.util;

import java.time.temporal.Temporal;
import java.util.function.Predicate;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.Column;
import org.minimalj.model.properties.Property;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.repository.query.Criteria;

public interface ColumnFilter extends Predicate<Object> {

	public static final ColumnFilter[] NO_FILTER = new ColumnFilter[0];
	
	public IComponent getComponent(InputComponentListener listener);

	public boolean active();
	
	public ValidationMessage validate();

	public Criteria getCriteria();
	
	public static ColumnFilter createFilter(Property property) {
		if (property instanceof Column) {
			ColumnFilter filter = ((Column<?, ?>) property).getFilter();
			if (filter != null) {
				return filter;
			}
		}
		Class<?> clazz = property.getClazz();
		if (Temporal.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz)) {
			return new ValueOrRangeColumnFilter(property);
		} else {
			return new StringColumnFilter(property);
		}
	}
	
}
