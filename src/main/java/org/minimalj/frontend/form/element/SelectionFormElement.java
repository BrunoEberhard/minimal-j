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

public class SelectionFormElement<T> extends AbstractFormElement<Selection<T>> {

	private final SwitchComponent component;
	private Input<T> input;

	public SelectionFormElement(Object key) {
		super(key);
		component = Frontend.getInstance().createSwitchComponent();
	}

	@Override
	public IComponent getComponent() {
		return component;
	}

	@Override
	public void setValue(Selection<T> selection) {
		List<T> values = selection.values != null ? selection.values : Collections.emptyList();
		component.show(input = Frontend.getInstance().createComboBox(values, listener()));
		T selectedValue = selection.selectedValue;
		if (selectedValue != null && !values.contains(selectedValue)) {
			Object id = IdUtils.getId(selectedValue);
			selectedValue = values.stream().filter(c -> Objects.equals(id, IdUtils.getId(c))).findFirst().orElse(null);
			input.setValue(selectedValue);
			fireChange();
		} else {
			input.setValue(selectedValue);
		}
	}

	@Override
	public Selection<T> getValue() {
		return new Selection<T>(input.getValue());
	}
}
