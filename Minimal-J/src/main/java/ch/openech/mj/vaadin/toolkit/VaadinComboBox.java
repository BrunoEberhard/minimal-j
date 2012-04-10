package ch.openech.mj.vaadin.toolkit;

import java.util.Collections;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.toolkit.ComboBox;

import com.vaadin.ui.Select;

public class VaadinComboBox<T> extends Select implements ComboBox<T> {

	private final ChangeListener listener;
	private List<T> objects = Collections.emptyList();
	private T setObject;
	
	public VaadinComboBox(ChangeListener listener) {
		setNullSelectionAllowed(true);
		setImmediate(true);
		this.listener = listener;
		addListener(new ComboBoxChangeListener());
	}
	
	@Override
	public void requestFocus() {
		super.focus();
	}

	@Override
	public void setObjects(List<T> objects) {
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
	public void setSelectedObject(T object) throws IllegalArgumentException {
		if (setObject != null && !setObject.equals(object) || setObject == null && object != null) {
			this.setObject = object;
			updateChoice();
		}
		super.setValue(object);
	}

	@Override
	public T getSelectedObject() {
		return (T) super.getValue();
	}

	public class ComboBoxChangeListener implements ValueChangeListener {

		@Override
		public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
			listener.stateChanged(new ChangeEvent(VaadinComboBox.this));
		}
	}

}
