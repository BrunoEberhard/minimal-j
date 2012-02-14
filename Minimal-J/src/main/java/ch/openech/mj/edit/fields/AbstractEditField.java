package ch.openech.mj.edit.fields;

import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.db.model.Constants;
import ch.openech.mj.edit.ChangeableValue;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.CheckBox;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ComboBox;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.IComponentDelegate;
import ch.openech.mj.toolkit.TextField;

public abstract class AbstractEditField<T> implements IComponentDelegate, EditField<T>, Indicator {

	private final String name;
	
	private ChangeListener forwardingChangeListener;
	private ChangeListener changeListener;
	private boolean adjusting = false;
	
	protected AbstractEditField(Object key) {
		this.name = Constants.getConstant(key);
	}

	@Override
	public String getName() {
		return name;
	}
	
	//
	
	@Override
	public boolean isEmpty() {
		return getObject() == null;
	}
	
	// Listener
	
	public void setAdjusting(boolean adjusting) {
		this.adjusting = adjusting;
	}

	protected void listenTo(TextField textField) {
		textField.setChangeListener(getForwardingChangeListener());
	}

	protected void listenTo(ComboBox comboBox) {
		comboBox.setChangeListener(getForwardingChangeListener());
	}

	protected void listenTo(CheckBox comboBox) {
		comboBox.setChangeListener(getForwardingChangeListener());
	}
	
	protected void listenTo(ChangeableValue<?> changeable) {
		changeable.setChangeListener(getForwardingChangeListener());
	}

	protected ChangeListener getForwardingChangeListener() {
		if (forwardingChangeListener == null) {
			forwardingChangeListener = new ForwardingChangeListener();
		}
		return forwardingChangeListener;
	}
	
	@Override
	public void setChangeListener(ChangeListener changeListener) {
		this.changeListener = changeListener;
	}

	protected void fireChange() {
		if (!adjusting && changeListener != null) {
			changeListener.stateChanged(new ChangeEvent(AbstractEditField.this));
		}
	}
	
	private class ForwardingChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			fireChange();
		}
	}
	
	//
	
	protected static void showBubble(IComponent component, String text) {
		ClientToolkit.getToolkit().showNotification(component, text);
	}
	
	//
	
//	@Override
//	public void setValidationMessages(List<ValidationMessage> validationMessages) {
//		if (getComponent() instanceof Indicator) {
//			Indicator indicator = (Indicator) getComponent();
//			indicator.setValidationMessages(validationMessages);
//		} else {
//			throw new RuntimeException("You must override setValidationMessages in " + this.getClass().getName());
//		}
//	}
}
