package org.minimalj.frontend.impl.util;

import java.util.List;
import java.util.Objects;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.properties.Property;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.repository.query.By;
import org.minimalj.repository.query.Criteria;

@SuppressWarnings({"rawtypes", "unchecked"})
public class TwoValueEnumColumnFilter implements ColumnFilter {

	private final Property property;
	private final List values;
	
	private Input component;
	
	public TwoValueEnumColumnFilter(Property property) {
		this.property = Objects.requireNonNull(property);
		if (!property.getClazz().isEnum()) {
			throw new IllegalArgumentException(property.getClazz().getName() + " is not an enum");
		}
		values = EnumUtils.valueList((Class) property.getClazz());
	}

	@Override
	public IComponent getComponent(InputComponentListener listener) {
		if (component == null) {
			component = Frontend.getInstance().createComboBox(values, listener);
		}
		return component;
	}

	@Override
	public boolean active() {
		return component.getValue() != null;
	}
	
	public ValidationMessage validate() {
		return null;
	}

	@Override
	public boolean test(Object object) {
		if (!active()) {
			return true;
		}
		Object value = property.getValue(object);
		return value == component.getValue();
	}

	@Override
	public Criteria getCriteria() {
		Object value = component.getValue();
		return By.field(property, value);
	}

}
