package org.minimalj.frontend.impl.util;

import java.util.Objects;
import java.util.function.Consumer;

import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.repository.query.By;
import org.minimalj.repository.query.Criteria;
import org.minimalj.util.ChangeListener;
import org.minimalj.util.StringUtils;

public class StringColumnFilter implements ColumnFilter {

	public static final StringColumnFilter $ = Keys.of(StringColumnFilter.class);

	public String string;

	private final PropertyInterface property;

	private ChangeListener<ColumnFilter> changeListener;

	public StringColumnFilter() {
		this.property = null;
	}

	public StringColumnFilter(PropertyInterface property, ChangeListener<ColumnFilter> changeListener) {
		this.property = property;
		this.changeListener = Objects.requireNonNull(changeListener);
	}

	@Override
	public PropertyInterface getProperty() {
		return property;
	}

	@Override
	public void runEditor(Consumer<String> finishedListener) {
		// not supported for strings
	}

	@Override
	public void setText(String string) {
		if (StringUtils.equals(string, this.string)) {
			return;
		}

		this.string = string;
		changeListener.changed(this);
	}

	@Override
	public String getText() {
		return string;
	}

	@Override
	public boolean active() {
		return !StringUtils.isEmpty(string);
	}

	@Override
	public boolean hasLookup() {
		return false;
	}
	
	private boolean exact() {
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
			if (!exact()) {
				String valueLowercase = this.string.toLowerCase();
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
		return By.search(!exact()  ? "%" + string + "%" : string.substring(1, string.length() - 1), property);
	}

}
