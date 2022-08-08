package org.minimalj.frontend.form.element;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.SwitchComponent;
import org.minimalj.model.Selection;
import org.minimalj.util.IdUtils;

public class SelectionFormElement<T> extends AbstractFormElement<Selection<T>> implements Enable {

	private final String nullText;
	private final SwitchComponent component;
	private Input<T> input;
	private boolean hasSelection;

	public SelectionFormElement(Object key) {
		this(key, null);
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
			input = Frontend.getInstance().createComboBox(Collections.emptyList(), nullText, listener());
			input.setEditable(false);
			component.show(input);
		}
	}

	@Override
	public Selection<T> getValue() {
		return new Selection<>(input.getValue());
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		input.setEditable(hasSelection && enabled);
	}
}
