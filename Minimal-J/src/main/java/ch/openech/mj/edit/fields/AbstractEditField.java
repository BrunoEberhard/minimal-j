package ch.openech.mj.edit.fields;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.toolkit.ClientToolkit.InputComponentListener;
import ch.openech.mj.toolkit.IComponent;

public abstract class AbstractEditField<T> implements EditField<T> {

	private final boolean editable;
	private final PropertyInterface property;
	
	private InputComponentListener forwardingChangeListener;
	private EditFieldListener changeListener;

	protected AbstractEditField(Object key, boolean editable) {
		this(Keys.getProperty(key), editable);
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
	
	protected InputComponentListener listener() {
		if (forwardingChangeListener == null) {
			forwardingChangeListener = new ForwardingChangeListener();
		}
		return forwardingChangeListener;
	}
	
	@Override
	public void setChangeListener(EditFieldListener changeListener) {
		this.changeListener = changeListener;
	}

	protected void fireChange() {
		if (changeListener != null) {
			changeListener.changed(AbstractEditField.this);
		}
	}
	
	private class ForwardingChangeListener implements InputComponentListener {
		@Override
		public void changed(IComponent source) {
			fireChange();
		}
	}

}
