package org.minimalj.frontend.form.element;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.SwitchComponent;
import org.minimalj.model.Selection;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.IdUtils;

public class SelectionFormElement<T> extends AbstractFormElement<Selection<T>> implements Enable, Indication {

	private final String nullText;
	private final SwitchComponent component;
	private Input<T> input;
	private boolean hasSelection;
	private List<T> values;

	public SelectionFormElement(Object key) {
		this(key, ComboBoxFormElement.NO_NULL_STRING);
	}
	
	public SelectionFormElement(Object key, String nullText) {
		super(key);
		this.nullText = nullText;
		component = Frontend.getInstance().createSwitchComponent();
	}

	@Override
	public IComponent getComponent() {
		return component;
	}

	@Override
	public void setValue(Selection<T> selection) {
		hasSelection = selection != null;
		if (selection != null) {
			this.values = selection.values;
			List<T> values = selection.values != null ? selection.values : Collections.emptyList();
			component.show(input = Frontend.getInstance().createComboBox(values, nullText, listener()));
			T selectedValue = selection.selectedValue;
			if (selectedValue != null && !values.contains(selectedValue) && IdUtils.hasId(selectedValue.getClass())) {
				Object id = IdUtils.getId(selectedValue);
				selectedValue = values.stream().filter(c -> Objects.equals(id, IdUtils.getId(c))).findFirst()
						.orElse(null);
				input.setValue(selectedValue);
				fireChange();
			} else {
				input.setValue(selectedValue);
			}
		} else {
			this.values = null;
			input = Frontend.getInstance().createComboBox(Collections.emptyList(), nullText, listener());
			input.setEditable(false);
			component.show(input);
		}
	}

	@Override
	public Selection<T> getValue() {
		return new Selection<>(input.getValue(), values);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		input.setEditable(hasSelection && enabled);
	}
	
	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages, FormContent formContent) {
		formContent.setValidationMessages(input, validationMessages.stream().map(ValidationMessage::getFormattedText).collect(Collectors.toList()));
	}
}
