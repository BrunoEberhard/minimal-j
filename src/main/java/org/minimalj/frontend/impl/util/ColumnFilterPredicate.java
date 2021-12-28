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
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public abstract class ColumnFilterPredicate implements Predicate<Object>, Rendering, InputComponentListener {
	protected final Class<?> clazz;
	private ChangeListener<String> listener;
	private String filterString;
	
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
	
	protected final void setFilterString(String filterString) {
		if (!StringUtils.equals(this.filterString, filterString)) {
			this.filterString = filterString;
			parseFilterString(filterString);
		}
	}
	
	protected abstract void parseFilterString(String filterString);

	public final String getFilterString() {
		return filterString;
	}
	
	public abstract boolean valid();

	public abstract IComponent getComponent();

	@Override
	public final void changed(IComponent source) {
		String filterString = formatFilterString();
		setFilterString(filterString);
		listener.changed(filterString);
	}
	
	protected abstract String formatFilterString();

	protected void copyFrom(ColumnFilterPredicate other) {
		
	}
	
	@Override
	public final boolean test(Object t) {
		if (t instanceof Collection) {
			return ((Collection) t).stream().anyMatch(this);
		} else {
			return valid() && doTest(t);
		}
	}
	
	protected abstract boolean doTest(Object t);

	public abstract Criteria getCriteria(PropertyInterface property);
	
	public static abstract class StringColumnFilterPredicate extends ColumnFilterPredicate {
		protected Input<String> input;
		private String editString;
		
		public StringColumnFilterPredicate(Class<?> clazz) {
			super(clazz);
		}
		
		@Override
		protected final void parseFilterString(String filterString) {
			editString = extractEditString(filterString);
		}
		
		protected abstract String extractEditString(String filterString);

		@Override
		public IComponent getComponent() {
			if (input == null) {
				input = Frontend.getInstance().createTextField(255, null, null, this);
			}
			input.setValue(editString);
			return input;
		}
		
		@Override
		protected void copyFrom(ColumnFilterPredicate other) {
			if (other instanceof StringColumnFilterPredicate) {
				editString = ((StringColumnFilterPredicate) other).editString;
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
			value = ComparableRange.parse(clazz, filterString, null);
			return filterString;
		}

		@Override
		protected String formatFilterString() {
			return input.getValue();
		}

		@Override
		public boolean valid() {
			return value != null && !InvalidValues.isInvalid(value);
		}

		@Override
		public boolean doTest(Object t) {
			return t != null ? ((Comparable) value).compareTo(t) == 0 : true;
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
		protected String extractEditString(String filterString) {
			value = new ComparableRange(clazz, filterString);
			return filterString;
		}

		@Override
		public boolean valid() {
			return value != null && ((ComparableRange) value).valid();
		}

		@Override
		public boolean doTest(Object t) {
			return t != null ? ((ComparableRange) value).test(t) : true;
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
		protected String extractEditString(String string) {
			if (!StringUtils.isEmpty(string)) {
				string = string.trim();
				if (string.endsWith("-")) {
					String editString = string.substring(0, string.length() - 1).trim();
					value = ComparableRange.parse(clazz, editString, false);
					return editString;
				}
			}
			value = null;
			return null;
		}

		@Override
		protected String formatFilterString() {
			return input.getValue() + " -";
		}

		@Override
		public boolean valid() {
			return value != null && !InvalidValues.isInvalid(value);
		}

		@Override
		public boolean doTest(Object t) {
			return t != null && !InvalidValues.isInvalid(value) ? value.compareTo(t) <= 0 : true;
		}

		@Override
		public Criteria getCriteria(PropertyInterface property) {
			return By.field(property, FieldOperator.greaterOrEqual, value);
		}
	}
	
	public static class MaxFilterPredicate extends StringColumnFilterPredicate {
		private Comparable value;

		public MaxFilterPredicate(Class<?> clazz) {
			super(clazz);
		}

		@Override
		protected String extractEditString(String string) {
			if (!StringUtils.isEmpty(string)) {
				string = string.trim();
				if (string.startsWith("-")) {
					String editString = string.substring(1).trim();
					value = ComparableRange.parse(clazz, editString, true);
					return editString;
				}
			}
			value = null;
			return null;
		}

		@Override
		protected String formatFilterString() {
			return "-" + input.getValue();
		}

		@Override
		public boolean valid() {
			return value != null && !InvalidValues.isInvalid(value);
		}

		@Override
		public boolean doTest(Object t) {
			return t != null && !InvalidValues.isInvalid(value) ? value.compareTo(t) >= 0 : true;
		}

		@Override
		public Criteria getCriteria(PropertyInterface property) {
			return By.field(property, FieldOperator.lessOrEqual, value);
		}
	}
	
	public static class RangeFilterPredicate extends ColumnFilterPredicate {
		private ComparableRange value;
		private String string1, string2;
		private IComponent component;
		private Input<String> input1, input2;
		
		public RangeFilterPredicate(Class<?> clazz) {
			super(clazz);
		}

		@Override
		protected void parseFilterString(String filterString) {
			if (filterString != null && filterString.indexOf("-") > -1) {
				int pos = filterString.indexOf("-");
				string1 = filterString.substring(0, pos).trim();
				string2 = filterString.substring(pos + 1).trim();
				value = new ComparableRange(clazz, string1, string2);
			} else {
				value = null;
				string1 = null;
				string2 = null;
			}
		}
		
		@Override
		public IComponent getComponent() {
			if (component == null) {
				input1 = Frontend.getInstance().createTextField(255, null, null, this);
				input2 = Frontend.getInstance().createTextField(255, null, null, this);
				component = Frontend.getInstance().createHorizontalGroup(input1, Frontend.getInstance().createText("  -  "), input2);
			}
			input1.setValue(string1);
			input2.setValue(string2);
			return component;
		}

		@Override
		protected String formatFilterString() {
			return input1.getValue() + " - " + input2.getValue();
		}

		@Override
		public boolean valid() {
			return value != null && value.valid();
		}

		@Override
		public boolean doTest(Object t) {
			return value != null && value.valid() && t != null ? value.test(t) : true;
		}
		
		@Override
		public Criteria getCriteria(PropertyInterface property) {
			return value.getCriteria(property);
		}
	}
}
