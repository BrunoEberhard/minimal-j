package org.minimalj.frontend.impl.util;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.FormContent;
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
	protected ChangeListener<String> listener;
	protected FormContent formContent;

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

	public void fillForm(FormContent formContent) {
		this.formContent = formContent;
	}

	@Override
	public final void changed(IComponent source) {
		voidUpdateInternalValueAndValidate();
		listener.changed(getFilterString());
	}

	private void voidUpdateInternalValueAndValidate() {
		updateInternalValue();
		validate();
	}
	
	protected abstract void updateInternalValue();

	protected abstract void validate();
	
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
		public void fillForm(FormContent formContent) {
			super.fillForm(formContent);
			formContent.add(Resources.getString("ColumnFilterModel.filterValue"), getInput(), null, 2);
		}

		private Input<String> getInput() {
			if (input == null) {
				input = Frontend.getInstance().createTextField(255, null, null, this);
			}
			return input;
		}
		
		@Override
		protected void validate() {
			if (!valid()) {
				String name = "TODO";
				formContent.setValidationMessages(input, List.of(MessageFormat.format(Resources.getString("ObjectValidator.message"), name)));
			} else {
				formContent.setValidationMessages(input, Collections.emptyList());
			}
		}

		@Override
		protected void copyFrom(ColumnFilterPredicate other) {
			if (other instanceof StringColumnFilterPredicate) {
				getInput().setValue(((StringColumnFilterPredicate) other).input.getValue());
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
			value = new ComparableRange(clazz);
		}

		@Override
		public CharSequence render() {
			return Resources.getString(EqualsFilterPredicate.class);
		}

		@Override
		protected void updateInternalValue() {
			((ComparableRange) value).setStringValue(input.getValue());
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
			return !StringUtils.isEmpty(input.getValue()) ? "> " + input.getValue() : null;
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
			return !StringUtils.isEmpty(input.getValue()) ? "< " + input.getValue() : null;
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
		private ComparableRange range;
		private Input<String> input1, input2;

		public RangeFilterPredicate(Class<?> clazz) {
			super(clazz);
			range = new ComparableRange(clazz);
		}

		@Override
		public boolean isFilterStringValid(String string) {
			return string != null && string.indexOf("-") > 0 && string.indexOf("-") < string.length() - 1;
		}

		@Override
		protected void setFilterString(String filterString) {
			initializeInputs();
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
			range.setStringValue1(input1.getValue());
			range.setStringValue2(input2.getValue());
		}

		private void initializeInputs() {
			if (input1 == null) {
				input1 = Frontend.getInstance().createTextField(255, null, null, this);
				input2 = Frontend.getInstance().createTextField(255, null, null, this);
			}
		}
		
		@Override
		protected void validate() {
			if (!valid()) {
				String name = "TODO";
				formContent.setValidationMessages(input1, List.of(MessageFormat.format(Resources.getString("ColumnFilterModel.filterValue1"), name)));
			} else {
				formContent.setValidationMessages(input1, Collections.emptyList());
			}
		}

		@Override
		public void fillForm(FormContent formContent) {
			super.fillForm(formContent);
			initializeInputs();
			formContent.add(Resources.getString("ColumnFilterModel.filterValue1"), input1, null, 1);
			formContent.add(Resources.getString("ColumnFilterModel.filterValue2"), input2, null, 1);
		}

		@Override
		protected String getFilterString() {
			StringBuilder s = new StringBuilder();
			if (!StringUtils.isEmpty(input1.getValue())) {
				s.append(input1.getValue());
			}
			s.append(" - ");
			if (!StringUtils.isEmpty(input2.getValue())) {
				s.append(input2.getValue());
			}
			return s.toString();
		}

		@Override
		public boolean valid() {
			return range != null && range.valid();
		}

		@Override
		public boolean doTest(Object t) {
			return t != null ? range.test(t) : false;
		}

		@Override
		public Criteria getCriteria(PropertyInterface property) {
			return range.getCriteria(property);
		}

		@Override
		protected void copyFrom(ColumnFilterPredicate other) {
			if (other instanceof MinFilterPredicate) {
				initializeInputs();
				input1.setValue(((MinFilterPredicate) other).input.getValue());
				input2.setValue("");
				updateInternalValue();
				listener.changed(getFilterString());
			} else if (other instanceof MaxFilterPredicate) {
				initializeInputs();
				input1.setValue("");
				input2.setValue(((MaxFilterPredicate) other).input.getValue());
				updateInternalValue();
				listener.changed(getFilterString());
			} else if (other instanceof EqualsFilterPredicate) {
				initializeInputs();
				input1.setValue(((EqualsFilterPredicate) other).input.getValue());
				input2.setValue("");
				updateInternalValue();
				listener.changed(getFilterString());
			} else {
				super.copyFrom(other);
			}
		}
	}
}
