package org.minimalj.frontend.impl.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.impl.util.ColumnFilterPredicate.EqualsFilterPredicate;
import org.minimalj.frontend.impl.util.ColumnFilterPredicate.MaxFilterPredicate;
import org.minimalj.frontend.impl.util.ColumnFilterPredicate.MinFilterPredicate;
import org.minimalj.frontend.impl.util.ColumnFilterPredicate.RangeFilterPredicate;
import org.minimalj.frontend.impl.util.ColumnFilterPredicate.TemporalEqualsFilterPredicate;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.Validation;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.repository.query.Criteria;

import com.microsoft.sqlserver.jdbc.StringUtils;

public class ValueOrRangeColumnFilter implements ColumnFilter {

	private final PropertyInterface property;

	private Input<String> component;
	private Input<String> textField;

	private final InputComponentListener listener;
	
	private boolean enabled;

	private List<ColumnFilterPredicate> columnFilterPredicates = new ArrayList<>();

	private ColumnFilterPredicate columnFilterPredicate;

	public ValueOrRangeColumnFilter(PropertyInterface property, InputComponentListener listener) {
		this.property = property;
		this.listener = Objects.requireNonNull(listener);

		addPredicates(property);
	}

	private void addPredicates(PropertyInterface property) {
		Class<?> clazz = property.getClazz();
		if (LocalDate.class == clazz || LocalTime.class == clazz || LocalDateTime.class == clazz) {
			columnFilterPredicates.add(new TemporalEqualsFilterPredicate(clazz));
		} else {
			columnFilterPredicates.add(new EqualsFilterPredicate(clazz));
		}
		columnFilterPredicates.add(new MinFilterPredicate(clazz));
		columnFilterPredicates.add(new MaxFilterPredicate(clazz));
		columnFilterPredicates.add(new RangeFilterPredicate(clazz));
	}

	@Override
	public boolean test(Object t) {
		Object value = property.getValue(t);
		if (columnFilterPredicate != null) {
			return columnFilterPredicate.test(value);
		} else {
			return true;
		}
	}

	@Override
	public PropertyInterface getProperty() {
		return property;
	}

	@Override
	public IComponent getComponent() {
		if (component == null) {
			InputComponentListener myListener = source -> {
				setFilterString(textField.getValue());
				listener.changed(source);
			};
			textField = Frontend.getInstance().createTextField(255, null, null, myListener);
			Consumer<String> finishedListener = string -> {
				textField.setValue(string);
				setFilterString(string);
				listener.changed(textField);
			};
			component = Frontend.getInstance().createLookup(textField, new ColumnFilterEditor(textField.getValue(), columnFilterPredicates, columnFilterPredicate, finishedListener));
		}
		return component;
	}
	
	private void setFilterString(String filterString) {
		columnFilterPredicate = null;
		for (int i = columnFilterPredicates.size() - 1; i >= 0; i--) {
			ColumnFilterPredicate columnFilterPredicate = columnFilterPredicates.get(i);
			columnFilterPredicate.setFilterString(filterString);
			if (columnFilterPredicate.valid()) {
				this.columnFilterPredicate = columnFilterPredicate;
				break;
			}
		}
	}
	
	@Override
	public boolean active() {
		return enabled && columnFilterPredicate != null;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Override
	public ValidationMessage validate() {
		if (enabled && !active() && textField != null && !StringUtils.isEmpty(textField.getValue())) {
			return Validation.createInvalidValidationMessage(property);
		} else {
			return null;
		}
	}

	@Override
	public Criteria getCriteria() {
		if (columnFilterPredicate != null) {
			return columnFilterPredicate.getCriteria(property);
		} else {
			return null;
		}
	}

}
