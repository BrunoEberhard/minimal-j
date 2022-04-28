package org.minimalj.frontend.impl.util;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public class ValueOrRangeColumnFilter implements ColumnFilter {

	private final PropertyInterface property;
	private final BiFunction<Object, Predicate<Object>, Boolean> tester;
	private final String name;
	
	private Input<String> component;
	private Input<String> textField;
	private ColumnFilterEditor editor;

	private List<ColumnFilterPredicate> columnFilterPredicates = new ArrayList<>();

	private ColumnFilterPredicate activeFilter;

	public ValueOrRangeColumnFilter(PropertyInterface property) {
		this.property = Objects.requireNonNull(property);
		this.tester = null;
		this.name = Resources.getPropertyName(property);
		addPredicates(property.getClazz());
	}

	public ValueOrRangeColumnFilter(Class<?> clazz, BiFunction<Object, Predicate<Object>, Boolean> tester, String name) {
		this.property = null;
		this.tester = Objects.requireNonNull(tester);
		this.name = Objects.requireNonNull(name);
		addPredicates(clazz);
	}

	protected void addPredicates(Class<?> clazz) {
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
		if (!active()) {
			return true;
		}
		if (property != null) {
			Object value = property.getValue(t);
			return activeFilter.test(value);
		} else if (tester != null) {
			return tester.apply(t, activeFilter);
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	public IComponent getComponent(InputComponentListener listener) {
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
			editor = new ColumnFilterEditor(name, textField, columnFilterPredicates, finishedListener);
			component = Frontend.getInstance().createLookup(textField, editor);
		}
		return component;
	}
	
	void setFilterString(String filterString) {
		activeFilter = null;
		for (int i = columnFilterPredicates.size() - 1; i >= 0; i--) {
			ColumnFilterPredicate columnFilterPredicate = columnFilterPredicates.get(i);
			if (columnFilterPredicate.isFilterStringValid(filterString)) {
				this.activeFilter = columnFilterPredicate;
				this.activeFilter.setFilterString(filterString);
				break;
			}
		}
	}
	
	@Override
	public final boolean active() {
		return activeFilter != null;
	}
	
	@Override
	public ValidationMessage validate() {
		if (activeFilter != null && !activeFilter.valid() && textField != null && !StringUtils.isEmpty(textField.getValue())) {
			if (property != null) {
				return Validation.createInvalidValidationMessage(property);
			} else {
				return new ValidationMessage(property, MessageFormat.format(Resources.getString("ObjectValidator.message"), name));
			}
		} else {
			return null;
		}
	}

	@Override
	public Criteria getCriteria() {
		if (activeFilter != null) {
			return activeFilter.getCriteria(property);
		} else {
			return null;
		}
	}

}
