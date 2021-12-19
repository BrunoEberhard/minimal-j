package org.minimalj.frontend.impl.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.function.Consumer;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.repository.query.By;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.FieldOperator;
import org.minimalj.util.ChangeListener;

public class GeneralColumnFilter implements ColumnFilter {

	public static final GeneralColumnFilter $ = Keys.of(GeneralColumnFilter.class);

	private final PropertyInterface property;

	private final ChangeListener<ColumnFilter> changeListener;
	
	private ValueOrRangeFilter filter;

	@Size(255)
	public String string;

	public GeneralColumnFilter() {
		this.property = null;
		this.changeListener = null;
	}

	public GeneralColumnFilter(PropertyInterface property, ChangeListener<ColumnFilter> changeListener) {
		this.property = property;
		this.changeListener = Objects.requireNonNull(changeListener);
	}

	@Override
	public PropertyInterface getProperty() {
		return property;
	}

	@Override
	public void runEditor(Consumer<String> finishedListener) {
		new ValueOrRangeFilterEditor(property, string, finishedListener).run();
	}
	
	@Override
	public void setText(String text) {
		this.string = text;
		filter = new ValueOrRangeFilter(property.getClazz(), text);
		changeListener.changed(this);
	}

	@Override
	public String getText() {
		return string;
	}

	@Override
	public boolean test(Object t) {
		if (filter != null) {
			return filter.test(property.getValue(t));
		} else {
			return true;
		}
	}
	
	@Override
	public boolean active() {
		if (filter != null) {
			return filter.active();
		} else {
			return false;
		}
	}
	
	@Override
	public boolean hasLookup() {
		return true;
	}

	public static boolean isAvailableFor(Class<?> clazz) {
		if (Integer.class == clazz)
			return true;
		if (Long.class == clazz)
			return true;
		if (Boolean.class == clazz)
			return true;
		if (BigDecimal.class == clazz)
			return true;
		if (LocalDate.class == clazz)
			return true;
		if (LocalTime.class == clazz)
			return true;
		if (LocalDateTime.class == clazz)
			return true;
		return false;
	}

	@Override
	public Criteria getCriteria() {
		ValueOrRangeFilter filter = new ValueOrRangeFilter(property.getClazz(), string);
		
		if (!active()) {
			return By.ALL;
		}
		FieldOperator fieldOperator = FieldOperator.equal;
		switch (filter.getOperator()) {
		case MIN:
			fieldOperator = FieldOperator.greaterOrEqual;
			break;
		case MAX:
			fieldOperator = FieldOperator.lessOrEqual;
			break;
		case RANGE:
			fieldOperator = FieldOperator.lessOrEqual;
			return By.field(property, FieldOperator.greaterOrEqual, filter.getValue1()).and(By.field(property, FieldOperator.lessOrEqual, filter.getValue2()));
		default:
			break;
		}
		return By.field(property, fieldOperator, filter.getValue1());
	}

}