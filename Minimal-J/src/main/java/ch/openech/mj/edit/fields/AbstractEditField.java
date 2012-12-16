package ch.openech.mj.edit.fields;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.db.model.Constants;
import ch.openech.mj.db.model.PropertyInterface;

public abstract class AbstractEditField<T> implements EditField<T> {

	private final boolean editable;
	private final PropertyInterface property;
	
	private ChangeListener forwardingChangeListener;
	private ChangeListener changeListener;

	protected AbstractEditField(Object key, boolean editable) {
		this(Constants.getProperty(key), editable);
	}
	
	protected AbstractEditField(PropertyInterface property, boolean editable) {
		if (property == null) throw new IllegalArgumentException();
		
		this.property = property;
		this.editable = editable;
	}

	protected boolean isEditable() {
		return editable;
	}
	
	@Override
	public PropertyInterface getProperty() {
		return property;
	}
	
	// Listener
	
	protected ChangeListener listener() {
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
		if (changeListener != null) {
			changeListener.stateChanged(new ChangeEvent(AbstractEditField.this));
		}
	}
	
	private class ForwardingChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			fireChange();
		}
	}

}
