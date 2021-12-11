package org.minimalj.frontend.impl.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.impl.util.GeneralColumnFilter.ColumnFilterFormElement;
import org.minimalj.frontend.impl.util.GeneralColumnFilter.ValueOrRange;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.DateUtils;

@SuppressWarnings("rawtypes")
public class GeneralFilterEditor extends SimpleEditor<GeneralColumnFilter> {

	private final GeneralColumnFilter filter;
	private final Consumer<String> finishedListener;

	public GeneralFilterEditor(GeneralColumnFilter filter, Consumer<String> finishedListener) {
		this.filter = filter;
		this.finishedListener = finishedListener;
	}

	@Override
	protected GeneralColumnFilter createObject() {
		return CloneHelper.clone(filter);
	}

	@Override
	protected Form<GeneralColumnFilter> createForm() {
		Form<GeneralColumnFilter> form = new Form<>(2);
		ColumnFilterFormElement valueOrRangeElement = createFormElement(GeneralColumnFilter.$.valueOrRange);
		valueOrRangeElement.setOperator(filter.operator);

		form.line(GeneralColumnFilter.$.operator);
		form.line(valueOrRangeElement);

		form.addDependecy(GeneralColumnFilter.$.operator, (operator, filter) -> valueOrRangeElement.setOperator(operator), GeneralColumnFilter.$.valueOrRange);
		return form;
	}

	private ColumnFilterFormElement createFormElement(ValueOrRange key) {
		return new ColumnFilterFormElement(key, filter.getProperty());
	}

	@Override
	protected GeneralColumnFilter save(GeneralColumnFilter filter) {
		CloneHelper.deepCopy(filter, this.filter);
		return filter;
	}

	@Override
	protected void finished(GeneralColumnFilter result) {
		String s = "";
		switch (result.operator) {
		case EQUALS:
			s = format(result.valueOrRange.value1);
			break;
		case MAX:
			s = "- " + format(result.valueOrRange.value1);
			break;
		case MIN:
			s = format(result.valueOrRange.value1) + " -";
			break;
		case RANGE:
			s = format(result.valueOrRange.value1) + " - " + format(result.valueOrRange.value2);
			break;
		default:
			break;
		}
		finishedListener.accept(s);
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
				DateTimeFormatter formatter = DateUtils.getTimeFormatter(filter.getProperty());
				return formatter.format((LocalTime) value);
			} else if (value instanceof LocalDateTime) {
				return DateUtils.format((LocalDateTime) value, filter.getProperty());
			} else {
				throw new IllegalArgumentException(value.toString());
			}
		} else {
			return "";
		}
	}

}
