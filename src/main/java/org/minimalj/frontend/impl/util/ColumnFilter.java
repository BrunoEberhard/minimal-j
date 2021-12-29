package org.minimalj.frontend.impl.util;

import java.time.temporal.Temporal;
import java.util.function.Predicate;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.Column;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.repository.query.Criteria;

public interface ColumnFilter extends Predicate<Object> {

	public IComponent getComponent(InputComponentListener listener);

	public void setEnabled(boolean enabled);
	
	public boolean active();
	
	public ValidationMessage validate();

	public Criteria getCriteria();
	
	public static ColumnFilter createFilter(PropertyInterface property) {
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
