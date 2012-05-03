package ch.openech.mj.edit.fields;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.db.model.Constants;
import ch.openech.mj.util.StringUtils;

public abstract class AbstractEditField<T> implements EditField<T> {

	private final boolean editable;
	private final String name;
	
	private ChangeListener forwardingChangeListener;
	private ChangeListener changeListener;
	
	protected AbstractEditField(Object key, boolean editable) {
		this.name = Constants.getConstant(key);
		this.editable = editable;
	}

	protected boolean isEditable() {
		return editable;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	//
	
	@Override
	public boolean isEmpty() {
		Object object = getObject();
		return object == null || (object instanceof String) && StringUtils.isEmpty((String) object);
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
