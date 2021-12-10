package org.minimalj.frontend.impl.util;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.SwitchComponent;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.AbstractFormElement;
import org.minimalj.frontend.form.element.BigDecimalFormElement;
import org.minimalj.frontend.form.element.FormElement;
import org.minimalj.frontend.form.element.IntegerFormElement;
import org.minimalj.frontend.form.element.LocalDateFormElement;
import org.minimalj.frontend.form.element.LocalDateTimeFormElement;
import org.minimalj.frontend.form.element.LocalTimeFormElement;
import org.minimalj.frontend.form.element.LongFormElement;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Decimal;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.Validation;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.ChangeListener;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.DateUtils;
import org.minimalj.util.StringUtils;

public class GeneralColumnFilter implements ColumnFilter {

	public static final GeneralColumnFilter $ = Keys.of(GeneralColumnFilter.class);

	private final PropertyInterface property;

	private final ChangeListener<ColumnFilter> changeListener;

	private ValidationMessage validationMessage = null;

	public ColumnFilterOperator operator = ColumnFilterOperator.EQUALS;

	@Size(255)
	public String string;

	public static class ValueOrRange {
		public static final ValueOrRange $ = Keys.of(ValueOrRange.class);

		public Comparable value1, value2;

		public void clear() {
			value1 = value2 = null;
		}
	}

	public final ValueOrRange valueOrRange = new ValueOrRange();

	public GeneralColumnFilter() {
		this.property = null;
		this.changeListener = null;
	}

	public GeneralColumnFilter(PropertyInterface property, ChangeListener<ColumnFilter> changeListener) {
		this.property = property;
		this.changeListener = Objects.requireNonNull(changeListener);
	}

	public boolean isRange() {
		return operator == ColumnFilterOperator.RANGE;
	}

	@Override
	public PropertyInterface getProperty() {
		return property;
	}

	@Override
	public void runEditor(Consumer<String> finishedListener) {
		new GeneralFilterEditor(this, finishedListener).run();
	}

	@Override
	public void setText(String string) {
		if (StringUtils.equals(string, this.string)) {
			return;
		}

		this.string = string;

		this.valueOrRange.clear();
		this.operator = ColumnFilterOperator.EQUALS;
		this.validationMessage = null;

		if (!StringUtils.isEmpty(string)) {
			try {
				if (string.startsWith("-")) {
					String toString = string.substring(1).trim();
					valueOrRange.value1 = parse(toString);
					operator = ColumnFilterOperator.MAX;
				} else if (string.endsWith("-")) {
					String fromString = string.substring(0, string.length() - 1).trim();
					valueOrRange.value1 = parse(fromString);
					operator = ColumnFilterOperator.MIN;
				} else if (string.indexOf("-") > -1) {
					int pos = string.indexOf("-");
					String fromString = string.substring(0, pos).trim();
					String toString = string.substring(pos + 1).trim();
					valueOrRange.value1 = parse(fromString);
					valueOrRange.value2 = parse(toString);
					operator = ColumnFilterOperator.RANGE;
				} else {
					valueOrRange.value1 = parse(string);
					operator = ColumnFilterOperator.EQUALS;
				}
			} catch (Exception x) {
				validationMessage = Validation.createInvalidValidationMessage($.string);
			}
		}

		changeListener.changed(this);
	}

	private Comparable parse(String string) {
		if (!StringUtils.isEmpty(string)) {
			Class<?> fieldClass = getProperty().getClazz();
			if (fieldClass == Integer.class) {
				return Integer.parseInt(string);
			} else if (fieldClass == Long.class) {
				return Long.parseLong(string);
			} else if (fieldClass == BigDecimal.class) {
				return new BigDecimal(string);
			} else if (fieldClass == LocalDate.class) {
				return DateUtils.parse(string);
//			} else if (fieldClass == LocalTime.class) {
//				return new LocalTimeFormElement(property, Form.EDITABLE);
//			} else if (fieldClass == LocalDateTime.class) {
//				return new LocalDateTimeFormElement(property, Form.EDITABLE);			
			} else {
				throw new IllegalArgumentException(fieldClass.getName());
			}
		} else {
			return null;
		}
	}

	@Override
	public String getText() {
		return string;
	}

	@Override
	public boolean active() {
		return !StringUtils.isEmpty(string) && validationMessage == null;
	}

	@Override
	public boolean hasLookup() {
		return true;
	}

	@Override
	public List<ValidationMessage> validate() {
		return validationMessage == null ? null : List.of(validationMessage);
	}

	@Override
	public boolean test(Object object) {
		if (valueOrRange.value1 == null) {
			return true;
		}

		Comparable v = (Comparable) getProperty().getValue(object);
		if (v != null) {
			switch (operator) {
			case EQUALS:
				return v.compareTo(valueOrRange.value1) == 0;
			case MAX:
				return v.compareTo(valueOrRange.value1) <= 0;
			case MIN:
				return v.compareTo(valueOrRange.value1) >= 0;
			case RANGE:
				return v.compareTo(valueOrRange.value1) >= 0 && v.compareTo(valueOrRange.value2) <= 0;
			default:
				return true;
			}
		} else {
			return false;
		}
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static class ColumnFilterFormElement extends AbstractFormElement<ValueOrRange> implements ChangeListener {

		private final PropertyInterface property;
		private final SwitchComponent switchComponent;
		private final IComponent rangeComponent;
		private final FormElement valueElement, fromElement, toElement;
		private ValueOrRange value;
		private ColumnFilterOperator operator;

		public ColumnFilterFormElement(ValueOrRange key, PropertyInterface property) {
			super(key);
			this.property = property;
			switchComponent = Frontend.getInstance().createSwitchComponent();
			fromElement = createFormElement(ValueOrRange.$.value1);
			toElement = createFormElement(ValueOrRange.$.value2);
			rangeComponent = Frontend.getInstance().createHorizontalGroup(fromElement.getComponent(), Frontend.getInstance().createText("  -  "), toElement.getComponent());
			valueElement = createFormElement(ValueOrRange.$.value1);

			fromElement.setChangeListener(this);
			toElement.setChangeListener(this);
			valueElement.setChangeListener(this);

			operator = ColumnFilterOperator.EQUALS;
			switchComponent.show(valueElement.getComponent());
		}

		private FormElement createFormElement(Object key) {
			Class fieldClass = property.getClazz();
			ColumnFilterFormProperty columnFilterFormProperty = new ColumnFilterFormProperty(super.getProperty(), Keys.getProperty(key), property);
			if (fieldClass == LocalDate.class) {
				return new LocalDateFormElement(columnFilterFormProperty, Form.EDITABLE);
			} else if (fieldClass == LocalTime.class) {
				return new LocalTimeFormElement(columnFilterFormProperty, Form.EDITABLE);
			} else if (fieldClass == LocalDateTime.class) {
				return new LocalDateTimeFormElement(columnFilterFormProperty, Form.EDITABLE);
			} else if (fieldClass == BigDecimal.class) {
				return new BigDecimalFormElement(columnFilterFormProperty, Form.EDITABLE);
			} else if (fieldClass == Integer.class) {
				return new IntegerFormElement(columnFilterFormProperty, Form.EDITABLE);
			} else if (fieldClass == Long.class) {
				return new LongFormElement(columnFilterFormProperty, Form.EDITABLE);
			} else {
				throw new IllegalArgumentException(fieldClass.getName());
			}
		}

		public ValueOrRange setOperator(ColumnFilterOperator operator) {
			if (operator == ColumnFilterOperator.RANGE && this.operator != ColumnFilterOperator.RANGE) {
				switchComponent.show(rangeComponent);
			} else if (operator != ColumnFilterOperator.RANGE && this.operator == ColumnFilterOperator.RANGE) {
				switchComponent.show(valueElement.getComponent());
			}
			this.operator = operator;
			return value;
		}

		@Override
		public void setValue(ValueOrRange value) {
			this.value = CloneHelper.clone(value);
			valueElement.setValue(value.value1);
			fromElement.setValue(value.value1);
			toElement.setValue(value.value2);
		}

		@Override
		public ValueOrRange getValue() {
			return value;
		}

		@Override
		public IComponent getComponent() {
			return switchComponent;
		}

		@Override
		public void changed(Object source) {
			if (operator != ColumnFilterOperator.RANGE) {
				value.value1 = (Comparable) valueElement.getValue();
			} else {
				value.value1 = (Comparable) fromElement.getValue();
				value.value2 = (Comparable) toElement.getValue();
			}
			fireChange();
		}

		private class ColumnFilterFormProperty extends ChainedProperty {
			private final PropertyInterface filteredProperty;

			public ColumnFilterFormProperty(PropertyInterface property1, PropertyInterface property2, PropertyInterface filteredProperty) {
				super(property1, property2);
				this.filteredProperty = Objects.requireNonNull(filteredProperty);
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
				if (annotationClass == Size.class || annotationClass == Decimal.class) {
					T annotation = filteredProperty.getAnnotation(annotationClass);
					// for read only getter the size is not mandatory
					// but for the filter form element a size is needed.
					if (annotation == null && annotationClass == Size.class) {
						annotation = (T) new Size() {
							@Override
							public Class<? extends Annotation> annotationType() {
								return Size.class;
							}

							@Override
							public int value() {
								return 255;
							}
						};
					}
					return annotation;
				} else {
					return super.getAnnotation(annotationClass);
				}
			}
		}
	}

}