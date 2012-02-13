package ch.openech.mj.vaadin.toolkit;

import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.ComboBox;

import com.vaadin.ui.Select;

public class VaadinComboBox extends Select implements ComboBox {

	private ComboBoxChangeListener changeListener;

	public VaadinComboBox() {
		setNullSelectionAllowed(false);
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

	@Override
	public void setChangeListener(ChangeListener listener) {
		if (changeListener == null) {
			changeListener = new ComboBoxChangeListener();
		}
		changeListener.setChangeListener(listener);
	}
	
	public class ComboBoxChangeListener implements ValueChangeListener {

		private ChangeListener changeListener;
		
		public void setChangeListener(ChangeListener changeListener) {
			if (changeListener == null) {
				if (this.changeListener != null) {
					removeListener(this);
				}
			} else {
				if (this.changeListener == null) {
					addListener(this);
				}
			}		
			this.changeListener = changeListener;
		}
		
		@Override
		public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
			changeListener.stateChanged(new ChangeEvent(VaadinComboBox.this));
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
