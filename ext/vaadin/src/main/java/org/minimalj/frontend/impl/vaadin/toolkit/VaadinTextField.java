package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;

/**
 * 
 * @author Bruno
 *
 */
public class VaadinTextField extends com.vaadin.ui.TextField implements Input<String> {
	private static final long serialVersionUID = 1L;

	private TextChangeEvent event;
	private Runnable commitListener;
	
	public VaadinTextField(InputComponentListener changeListener, int maxLength) {
		this(changeListener, maxLength, null);
	}
	
	public VaadinTextField(InputComponentListener changeListener, int maxLength, String allowedCharacters) {
		setMaxLength(maxLength);
		setNullRepresentation("");
		setImmediate(true);
		if (changeListener != null) {
			addListener(new VaadinTextFieldTextChangeListener(changeListener));
			addShortcutListener(new ShortcutListener("Commit", ShortcutAction.KeyCode.ENTER, null) {
				private static final long serialVersionUID = 1L;

				@Override
				public void handleAction(Object sender, Object target) {
					if (target == VaadinTextField.this) {
						if (commitListener != null) {
							commitListener.run();
						}
					}
				}
			});
		} else {
			setReadOnly(true);
		}
	}

	public void setCommitListener(Runnable commitListener) {
		this.commitListener = commitListener;
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
