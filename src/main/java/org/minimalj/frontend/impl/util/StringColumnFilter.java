package org.minimalj.frontend.impl.util;

import java.util.Objects;
import java.util.function.Consumer;

import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.ChangeListener;
import org.minimalj.util.StringUtils;

public class StringColumnFilter implements ColumnFilter {

	public static final StringColumnFilter $ = Keys.of(StringColumnFilter.class);

	public String string;

	public Boolean exakt;

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

	@Override
	public boolean test(Object object) {
		if (!active()) {
			return true;
		}
		String valueLowercase = this.string.toLowerCase();

		Object value = getProperty().getValue(object);
		if (value instanceof Rendering) {
			value = ((Rendering) value).render();
		}
		if (value != null) {
			if (!Boolean.TRUE.equals(exakt)) {
				return value.toString().toLowerCase().contains(valueLowercase);
			} else {
				return value.toString().toLowerCase().equals(valueLowercase);
			}
		} else {
			return false;
		}

	}

}
