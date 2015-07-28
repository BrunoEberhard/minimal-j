package org.minimalj.frontend.vaadin.toolkit;

import java.util.List;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;

import com.vaadin.ui.Select;

public class VaadinComboBox<T> extends Select implements Input {
	private static final long serialVersionUID = 1L;
	
	private final InputComponentListener listener;
	private final List<T> objects;
	private T setObject;
	
	public VaadinComboBox(List<T> objects, InputComponentListener listener) {
		this.listener = listener;
		addListener(new ComboBoxChangeListener());

		setImmediate(true);
		setNullSelectionAllowed(true);

		this.objects = objects;
		updateChoice();
	}
	
	private void updateChoice() {
		removeAllItems();
		if (setObject != null && !objects.contains(setObject)) {
			addItem(setObject);
		}
		for (Object object : objects) {
			addItem(object);
		}
	}

	@Override
	public void setValue(Object object) throws IllegalArgumentException {
		if (setObject != null && !setObject.equals(object) || setObject == null && object != null) {
			this.setObject = (T) object;
			updateChoice();
		}
		super.setValue(object);
	}

	@Override
	public T getValue() {
		return (T) super.getValue();
	}

	public class ComboBoxChangeListener implements ValueChangeListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
			listener.changed(VaadinComboBox.this);
		}
	}

	@Override
	public void setEditable(boolean editable) {
		setEnabled(editable);
	}

}
