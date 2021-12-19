 package org.minimalj.frontend.impl.util;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.SwitchComponent;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.AbstractFormElement;
import org.minimalj.frontend.form.element.StringFormElement;
import org.minimalj.frontend.impl.util.ColumnFilter.ColumnFilterOperator;
import org.minimalj.frontend.impl.util.ValueOrRangeFilterEditor.ValueOrRangeFilterModel;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Decimal;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.model.validation.Validation;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.ChangeListener;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.DateUtils;
import org.minimalj.util.StringUtils;

@SuppressWarnings("rawtypes")
public class ValueOrRangeFilterEditor extends SimpleEditor<ValueOrRangeFilterModel> {

	private final PropertyInterface property;
	private final ValueOrRangeFilter filter;
	private final Consumer<String> finishedListener;

	public ValueOrRangeFilterEditor(PropertyInterface property, String string, Consumer<String> finishedListener) {
		this.property = property;
		this.filter = new ValueOrRangeFilter(property.getClazz(), string);
		this.finishedListener = finishedListener;
	}
	
	public static class ValueOrRange {
		public static final ValueOrRange $ = Keys.of(ValueOrRange.class);

		@Size(255)
		public String string1, string2;
	}

	public static class ValueOrRangeFilterModel {
		public static final ValueOrRangeFilterModel $ = Keys.of(ValueOrRangeFilterModel.class);

		public ColumnFilterOperator operator = ColumnFilterOperator.EQUALS;
		
		public final ValueOrRange valueOrRange = new ValueOrRange();
	}

	@Override
	protected ValueOrRangeFilterModel createObject() {
		ValueOrRangeFilterModel model = new ValueOrRangeFilterModel();
		model.valueOrRange.string1 = filter.getString1();
		model.valueOrRange.string2 = filter.getString2();
		if (filter.getOperator() == ColumnFilterOperator.RANGE && StringUtils.equals(model.valueOrRange.string1, model.valueOrRange.string2)) {
			model.operator = ColumnFilterOperator.EQUALS;
		} else if (filter.getOperator() != null) {
			model.operator = filter.getOperator();
		} else {
			model.operator = ColumnFilterOperator.EQUALS;
		}
		return model;
	}

	@Override
	protected Form<ValueOrRangeFilterModel> createForm() {
		Form<ValueOrRangeFilterModel> form = new Form<>(2);
		ColumnFilterFormElement valueOrRangeElement = new ColumnFilterFormElement(ValueOrRangeFilterModel.$.valueOrRange, property);
		valueOrRangeElement.setOperator(getObject().operator);

		form.line(ValueOrRangeFilterModel.$.operator);
		form.line(valueOrRangeElement);

		form.addDependecy(ValueOrRangeFilterModel.$.operator, (operator, filter) -> valueOrRangeElement.setOperator(operator), ValueOrRangeFilterModel.$.valueOrRange);
		return form;
	}

	@Override
	protected void validate(ValueOrRangeFilterModel model, List<ValidationMessage> validationMessages) {
		Object value1 = ValueOrRangeFilter.parse(property.getClazz(), model.valueOrRange.string1, false);
		if (InvalidValues.isInvalid(value1)) {
			validationMessages.add(Validation.createInvalidValidationMessage(ValueOrRangeFilterModel.$.valueOrRange.string1));
		}
		if (model.operator == ColumnFilterOperator.RANGE) {
			Object value2 = ValueOrRangeFilter.parse(property.getClazz(), model.valueOrRange.string2, false);
			if (InvalidValues.isInvalid(value2)) {
				validationMessages.add(Validation.createInvalidValidationMessage(ValueOrRangeFilterModel.$.valueOrRange.string2));
			}
		}
	}
	
	@Override
	protected ValueOrRangeFilterModel save(ValueOrRangeFilterModel filter) {
		String s = "";
		switch (filter.operator) {
		case EQUALS:
			s = filter.valueOrRange.string1;
			break;
		case MAX:
			s = "- " + filter.valueOrRange.string1;
			break;
		case MIN:
			s = filter.valueOrRange.string1 + " -";
			break;
		case RANGE:
			if (!StringUtils.equals(filter.valueOrRange.string1, filter.valueOrRange.string2)) {
				s = filter.valueOrRange.string1 + " - " + filter.valueOrRange.string2;
			} else {
				s = filter.valueOrRange.string1;
			}
			break;
		default:
			break;
		}
		finishedListener.accept(s);
		return filter;
	}
	
	private String format(Comparable value) {
		if (value != null) {
			if (value instanceof Integer || value instanceof Long) {
				return value.toString();
			} else if (value instanceof BigDecimal) {
				return ((BigDecimal) value).toPlainString();
			} else if (value instanceof LocalDate) {
				return DateUtils.format((LocalDate) value);
			} else if (value instanceof LocalTime) {
				DateTimeFormatter formatter = DateUtils.getTimeFormatter(property);
				return formatter.format((LocalTime) value);
			} else if (value instanceof LocalDateTime) {
				return DateUtils.format((LocalDateTime) value, property);
			} else {
				throw new IllegalArgumentException(value.toString());
			}
		} else {
			return "";
		}
	}

	public static class ColumnFilterFormElement extends AbstractFormElement<ValueOrRange> implements ChangeListener {

		private final PropertyInterface property;
		private final SwitchComponent switchComponent;
		private final IComponent rangeComponent;
		private final StringFormElement valueElement, fromElement, toElement;
		private ValueOrRange value;
		private ColumnFilterOperator operator;

		public ColumnFilterFormElement(ValueOrRange key, PropertyInterface property) {
			super(key);
			this.property = property;
			switchComponent = Frontend.getInstance().createSwitchComponent();
			fromElement = createFormElement(ValueOrRange.$.string1);
			toElement = createFormElement(ValueOrRange.$.string2);
			rangeComponent = Frontend.getInstance().createHorizontalGroup(fromElement.getComponent(), Frontend.getInstance().createText("  -  "), toElement.getComponent());
			valueElement = createFormElement(ValueOrRange.$.string1);

			fromElement.setChangeListener(this);
			toElement.setChangeListener(this);
			valueElement.setChangeListener(this);

			operator = ColumnFilterOperator.EQUALS;
			switchComponent.show(valueElement.getComponent());
		}

		private StringFormElement createFormElement(Object key) {
			ColumnFilterFormProperty columnFilterFormProperty = new ColumnFilterFormProperty(super.getProperty(), Keys.getProperty(key), property);
			return new StringFormElement(columnFilterFormProperty);
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
			valueElement.setValue(value.string1);
			fromElement.setValue(value.string1);
			toElement.setValue(value.string2);
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
				value.string1 = valueElement.getValue();
			} else {
				value.string1 = fromElement.getValue();
				value.string2 = toElement.getValue();
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
								// TODO specific for LocalDate, Time
								return 255;
							}
						};
					}
					return annotation;
				} else {
					return null;
				}
			}
		}
	}


}
