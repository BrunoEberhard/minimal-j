package org.minimalj.frontend.impl.util;

import java.util.Objects;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.repository.query.By;
import org.minimalj.repository.query.Criteria;
import org.minimalj.util.StringUtils;

public class StringColumnFilter implements ColumnFilter {

	public static final StringColumnFilter $ = Keys.of(StringColumnFilter.class);

	private final PropertyInterface property;

	private Input<String> component;
	
	private InputComponentListener listener;

	public StringColumnFilter() {
		this.property = null;
	}

	public StringColumnFilter(PropertyInterface property, InputComponentListener listener) {
		this.property = property;
		this.listener = Objects.requireNonNull(listener);
	}

	@Override
	public PropertyInterface getProperty() {
		return property;
	}

	@Override
	public IComponent getComponent() {
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

	@Override
	public boolean test(Object object) {
		if (!active()) {
			return true;
		}

		Object value = getProperty().getValue(object);
		if (value instanceof Rendering) {
			value = ((Rendering) value).render();
		}
		if (value != null) {
			String string = component.getValue();
			if (!exact()) {
				String valueLowercase = string.toLowerCase();
				return value.toString().toLowerCase().contains(valueLowercase);
			} else {
				return value.toString().equals(string.substring(1, string.length() - 1));
			}
		} else {
			return false;
		}
	}

	@Override
	public Criteria getCriteria() {
		String string = component.getValue();
		return By.search(!exact()  ? "%" + string + "%" : string.substring(1, string.length() - 1), property);
	}

}
