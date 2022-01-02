package org.minimalj.frontend.impl.util;

import java.util.Collection;
import java.util.function.Predicate;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.Rendering;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.repository.query.By;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.FieldOperator;
import org.minimalj.util.ChangeListener;
import org.minimalj.util.resources.Resources;

public abstract class ColumnFilterPredicate implements Predicate<Object>, Rendering, InputComponentListener {
	protected final Class<?> clazz;
	protected ChangeListener<String> listener;
	
	public ColumnFilterPredicate(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	@Override
	public CharSequence render() {
		return Resources.getString(this.getClass());
	}

	public void setChangeListener(ChangeListener<String> listener) {
		this.listener = listener;
	}

	protected abstract String getFilterString();
	
	protected abstract void setFilterString(String filterString);

	public abstract boolean isFilterStringValid(String filterString);
	
	public abstract boolean valid();

	public abstract IComponent getComponent();

	@Override
	public final void changed(IComponent source) {
		updateInternalValue();
		listener.changed(getFilterString());
	}
	
	protected abstract void updateInternalValue();

	protected void copyFrom(ColumnFilterPredicate other) {
		
	}
	
	@Override
	public final boolean test(Object t) {
		if (t instanceof Collection) {
			return ((Collection) t).stream().anyMatch(this);
		} else {
			return !valid() || doTest(t);
		}
	}
	
	protected abstract boolean doTest(Object t);

	public abstract Criteria getCriteria(PropertyInterface property);
	
	public static abstract class StringColumnFilterPredicate extends ColumnFilterPredicate {
		protected Input<String> input;
		
		public StringColumnFilterPredicate(Class<?> clazz) {
			super(clazz);
		}
		
		@Override
		protected final void setFilterString(String filterString) {
			String extractEditString = extractEditString(filterString);
			getInput().setValue(extractEditString);
			updateInternalValue();
		}
		
		protected abstract String extractEditString(String filterString);

		@Override
		public IComponent getComponent() {
			return getInput();
		}

		private Input<String> getInput() {
			if (input == null) {
				input = Frontend.getInstance().createTextField(255, null, null, this);
			}
			return input;
		}
		
		@Override
		protected void copyFrom(ColumnFilterPredicate other) {
			if (other instanceof StringColumnFilterPredicate) {
				input.setValue(((StringColumnFilterPredicate) other).input.getValue());
				updateInternalValue();
				listener.changed(getFilterString());
			}
		}
	}

	public static class EqualsFilterPredicate extends StringColumnFilterPredicate {
		protected Object value;

		public EqualsFilterPredicate(Class<?> clazz) {
			super(clazz);
		}

		@Override
		protected String extractEditString(String filterString) {
			return filterString;
		}

		@Override
		public boolean isFilterStringValid(String filterString) {
			return true;
		}
		
		@Override
		protected void updateInternalValue() {
			value = ComparableRange.parse(clazz, input.getValue(), null);
		}
		
		@Override
		protected String getFilterString() {
			return input.getValue();
		}

		@Override
		public boolean valid() {
			return value != null && !InvalidValues.isInvalid(value);
		}

		@Override
		public boolean doTest(Object t) {
			return t != null ? ((Comparable) value).compareTo(t) == 0 : false;
		}

		@Override
		public Criteria getCriteria(PropertyInterface property) {
			return By.field(property, value);
		}
	}
	
	public static class TemporalEqualsFilterPredicate extends EqualsFilterPredicate {
	
		public TemporalEqualsFilterPredicate(Class<?> clazz) {
			super(clazz);
		}
		
		@Override
		public CharSequence render() {
			return Resources.getString(EqualsFilterPredicate.class);
		}

		@Override
		protected void updateInternalValue() {
			value = new ComparableRange(clazz, input.getValue());
		}
		
		@Override
		public boolean valid() {
			return value != null && ((ComparableRange) value).valid();
		}

		@Override
		public boolean doTest(Object t) {
			return t != null ? ((ComparableRange) value).test(t) : false;
		}

		@Override
		public Criteria getCriteria(PropertyInterface property) {
			return ((ComparableRange) value).getCriteria(property);
		}
	}

	public static class MinFilterPredicate extends StringColumnFilterPredicate {
		private Comparable value;

		public MinFilterPredicate(Class<?> clazz) {
			super(clazz);
		}

		@Override
		public boolean isFilterStringValid(String string) {
			return string != null && string.startsWith(">");
		}
		
		@Override
		protected String extractEditString(String string) {
			if (isFilterStringValid(string)) {
				return string.substring(1).trim();
			} else {
				return null;
			}
		}

		@Override
		protected void updateInternalValue() {
			value = ComparableRange.parse(clazz, input.getValue(), false);
		}
		
		@Override
		protected String getFilterString() {
			return "> " + input.getValue();
		}

		@Override
		public boolean valid() {
			return value != null && !InvalidValues.isInvalid(value);
		}

		@Override
		public boolean doTest(Object t) {
			return t != null ? value.compareTo(t) <= 0 : false;
		}

		@Override
		public Criteria getCriteria(PropertyInterface property) {
			return By.field(property, FieldOperator.greaterOrEqual, value);
		}
		
		@Override
		protected void copyFrom(ColumnFilterPredicate other) {
			if (other instanceof RangeFilterPredicate) {
				input.setValue(((RangeFilterPredicate) other).input1.getValue());
				updateInternalValue();
				listener.changed(getFilterString());
			} else {
				super.copyFrom(other);
			}
		}
	}
	
	public static class MaxFilterPredicate extends StringColumnFilterPredicate {
		private Comparable value;

		public MaxFilterPredicate(Class<?> clazz) {
			super(clazz);
		}

		@Override
		public boolean isFilterStringValid(String string) {
			return string != null && string.startsWith("<");
		}
		
		@Override
		protected String extractEditString(String string) {
			if (isFilterStringValid(string)) {
				return string.substring(1).trim();
			} else {
				return null;
			}
		}

		@Override
		protected void updateInternalValue() {
			value = ComparableRange.parse(clazz, input.getValue(), true);
		}

		@Override
		protected String getFilterString() {
			return "< " + input.getValue();
		}

		@Override
		public boolean valid() {
			return value != null && !InvalidValues.isInvalid(value);
		}

		@Override
		public boolean doTest(Object t) {
			return t != null ? value.compareTo(t) >= 0 : true;
		}

		@Override
		public Criteria getCriteria(PropertyInterface property) {
			return By.field(property, FieldOperator.lessOrEqual, value);
		}
		
		@Override
		protected void copyFrom(ColumnFilterPredicate other) {
			if (other instanceof RangeFilterPredicate) {
				input.setValue(((RangeFilterPredicate) other).input2.getValue());
				updateInternalValue();
				listener.changed(getFilterString());
			} else {
				super.copyFrom(other);
			}
		}
	}
	
	public static class RangeFilterPredicate extends ColumnFilterPredicate {
		private ComparableRange value;
		private IComponent component;
		private Input<String> input1, input2;
		
		public RangeFilterPredicate(Class<?> clazz) {
			super(clazz);
		}

		@Override
		public boolean isFilterStringValid(String string) {
			return string != null && string.indexOf("-") > 0;
		}

		@Override
		protected void setFilterString(String filterString) {
			getComponent(); // initialize components
			if (isFilterStringValid(filterString)) {
				int pos = filterString.indexOf("-");
				input1.setValue(filterString.substring(0, pos).trim());
				input2.setValue(filterString.substring(pos + 1).trim());
			} else {
				input1.setValue(null);
				input2.setValue(null);
			}
			updateInternalValue();
		}
		
		@Override
		protected void updateInternalValue() {
			value = new ComparableRange(clazz, input1.getValue(), input2.getValue());
		}
		
		@Override
		public IComponent getComponent() {
			if (component == null) {
				input1 = Frontend.getInstance().createTextField(255, null, null, this);
				input2 = Frontend.getInstance().createTextField(255, null, null, this);
				component = Frontend.getInstance().createHorizontalGroup(input1, Frontend.getInstance().createText("  -  "), input2);
			}
			return component;
		}

		@Override
		protected String getFilterString() {
			return input1.getValue() + " - " + input2.getValue();
		}

		@Override
		public boolean valid() {
			return value != null && value.valid();
		}

		@Override
		public boolean doTest(Object t) {
			return value != null ? value.test(t) : false;
		}
		
		@Override
		public Criteria getCriteria(PropertyInterface property) {
			return value.getCriteria(property);
		}
		
		@Override
		protected void copyFrom(ColumnFilterPredicate other) {
			if (other instanceof MinFilterPredicate) {
				input1.setValue(((MinFilterPredicate) other).input.getValue());
				updateInternalValue();
				listener.changed(getFilterString());
			} else if (other instanceof MaxFilterPredicate) {
				input2.setValue(((MaxFilterPredicate) other).input.getValue());
				updateInternalValue();
				listener.changed(getFilterString());
			} else {
				super.copyFrom(other);
			}
		}
	}
}
