package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.action.Action;

import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.v7.ui.AbstractTextField;
import com.vaadin.ui.Component;

/**
 * 
 * @author Bruno
 *
 */
public class VaadinTextField extends com.vaadin.v7.ui.TextField implements Input<String> {
	private static final long serialVersionUID = 1L;

	private TextChangeEvent event;
	
	public VaadinTextField(InputComponentListener changeListener, int maxLength) {
		this(changeListener, maxLength, null);
	}
	
	public VaadinTextField(InputComponentListener changeListener, int maxLength, String allowedCharacters) {
		setMaxLength(maxLength);
		setNullRepresentation("");
		setImmediate(true);
		if (changeListener != null) {
			addTextChangeListener(new VaadinTextFieldTextChangeListener(changeListener));
			addShortcutListener(new CommitShortcutListener());
		} else {
			setReadOnly(true);
		}
	}
	
	static class CommitShortcutListener extends ShortcutListener {
		private static final long serialVersionUID = 1L;

		public CommitShortcutListener() {
			super("Commit", ShortcutAction.KeyCode.ENTER, null);
		}
		
		@Override
		public void handleAction(Object sender, Object target) {
			if (target instanceof AbstractTextField) {
				VaadinDialog dialog = findDialog((AbstractTextField) target);
				if (dialog != null) {
					Action saveAction = dialog.getSaveAction();
					if (saveAction.isEnabled()) {
						saveAction.action();
					}						
				}
			}
		}
	}

	static VaadinDialog findDialog(Component c) {
		while (c != null) {
			if (c instanceof VaadinDialog) {
				return (VaadinDialog) c;
			}
			c = c.getParent();
		}
		return null;
	}
	
	private class VaadinTextFieldTextChangeListener implements TextChangeListener {
		private static final long serialVersionUID = 1L;
		private final InputComponentListener changeListener;
		
		public VaadinTextFieldTextChangeListener(InputComponentListener changeListener) {
			this.changeListener = changeListener;
		}

		@Override
		public void textChange(TextChangeEvent event) {
			VaadinTextField.this.event = event;
			changeListener.changed(VaadinTextField.this);
			VaadinTextField.this.event = null;
		}
	}

	@Override
	public void setEditable(boolean editable) {
		setReadOnly(!editable);
	}

	@Override
	public void setValue(String text) {
		boolean readOnly = isReadOnly();
		if (readOnly) {
			setReadOnly(false);
			super.setValue(text);
			setReadOnly(true);
		} else {
			super.setValue(text);
		}		
	}

	@Override
	public String getValue() {
		if (event != null) {
			return event.getText();
		} else {
			return super.getValue();
		}
	}
	
}
