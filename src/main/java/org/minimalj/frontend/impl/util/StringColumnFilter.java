package org.minimalj.frontend.impl.util;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.Column;
import org.minimalj.model.Rendering;
import org.minimalj.model.properties.Property;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.repository.query.By;
import org.minimalj.repository.query.Criteria;
import org.minimalj.util.StringUtils;

public class StringColumnFilter implements ColumnFilter {

	private final Property property;
	private final BiFunction<Object, Predicate<String>, Boolean> tester;
	
	private Input<String> component;
	
	public StringColumnFilter(Property property) {
		this.property = Objects.requireNonNull(property);
		this.tester = null;
	}
	
	public StringColumnFilter(BiFunction<Object, Predicate<String>, Boolean> tester) {
		this.property = null;
		this.tester = Objects.requireNonNull(tester);
	}

	@Override
	public IComponent getComponent(InputComponentListener listener) {
		if (component == null) {
			component = Frontend.getInstance().createTextField(255, null, null, listener);
		}
		return component;
	}

	@Override
	public boolean active() {
		return !StringUtils.isEmpty(component.getValue());
	}
	
	public ValidationMessage validate() {
		return null;
	}

	private boolean exact() {
		String string = component.getValue();
		return string != null && string.length() > 2 && string.charAt(0) == '"' && string.charAt(string.length()-1) == '"';
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean test(Object object) {
		if (!active()) {
			return true;
		}

		String string = component.getValue();
		Predicate<String> predicate = value -> {
			if (!exact()) {
				String valueLowercase = string.toLowerCase();
				return value.toLowerCase().contains(valueLowercase);
			} else {
				return value.equals(string.substring(1, string.length() - 1));
			}
		};
		
		if (property != null) {
			Object value = property.getValue(object);
			if (property instanceof Column) {
				@SuppressWarnings("rawtypes")
				Column column = (Column) property;
				value = Rendering.toString(column.render(object, value));
			} else if (value instanceof Rendering) {
				value = ((Rendering) value).render();
			} else if (value instanceof Enum) {
				value = Rendering.toString((Enum<?>) value);
			}
			return value != null ? predicate.test(value.toString()) : false;
		} else if (tester != null) {
			return tester.apply(object, predicate);
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	public Criteria getCriteria() {
		String string = component.getValue();
		return By.search(!exact()  ? "%" + string + "%" : string.substring(1, string.length() - 1), property);
	}

}
