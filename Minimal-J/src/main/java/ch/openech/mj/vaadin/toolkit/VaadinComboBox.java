package ch.openech.mj.vaadin.toolkit;

import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.ComboBox;

import com.vaadin.ui.Select;

public class VaadinComboBox extends Select implements ComboBox {

	private final ChangeListener listener;

	public VaadinComboBox(ChangeListener listener) {
		setNullSelectionAllowed(false);
		setImmediate(true);
		this.listener = listener;
		addListener(new ComboBoxChangeListener());
	}
	
	@Override
	public void requestFocus() {
		super.focus();
	}

	@Override
	public void setObjects(List<?> objects) {
		removeAllItems();
		for (Object object : objects) {
			addItem(object);
		}
	}

	@Override
	public void setSelectedObject(Object object) throws IllegalArgumentException {
		super.setValue(object);
	}

	@Override
	public Object getSelectedObject() {
		return super.getValue();
	}

	public class ComboBoxChangeListener implements ValueChangeListener {

		@Override
		public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
			listener.stateChanged(new ChangeEvent(VaadinComboBox.this));
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		VaadinIndication.setValidationMessages(validationMessages, this);
	}

}
