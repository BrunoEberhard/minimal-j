package org.minimalj.frontend.impl.vaadin.toolkit;

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
		setNullSelectionAllowed(true);
		setImmediate(true);
		this.listener = listener;
		this.objects = objects;
		updateChoice();
		addListener(new ComboBoxChangeListener());
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
	public void setValue(Object object) {
		if (setObject != null && !setObject.equals(object) || setObject == null && object != null) {
			this.setObject = (T) object;
			updateChoice();
		}
		super.setValue(object);
	}

	@Override
	public Object getValue() {
		return super.getValue();
	}
	
	@Override
	public void setEditable(boolean editable) {
		super.setEnabled(editable);
	}

	public class ComboBoxChangeListener implements ValueChangeListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
			listener.changed(VaadinComboBox.this);
		}
	}

}
