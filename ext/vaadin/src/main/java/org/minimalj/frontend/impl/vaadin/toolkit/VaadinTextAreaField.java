package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;

/**
 * Nearly identical with TextField but extends from TextArea.
 * Maybe some code could be deduplicated.
 * 
 */
public class VaadinTextAreaField extends com.vaadin.ui.TextArea implements Input<String> {
	private static final long serialVersionUID = 1L;

	private TextChangeEvent event;
	private Runnable commitListener;
	
	public VaadinTextAreaField(InputComponentListener changeListener, int maxLength) {
		this(changeListener, maxLength, null);
	}
	
	public VaadinTextAreaField(InputComponentListener changeListener, int maxLength, String allowedCharacters) {
		setMaxLength(maxLength);
		setNullRepresentation("");
		setImmediate(true);
		if (changeListener != null) {
			addListener(new VaadinTextFieldTextChangeListener(changeListener));
			addShortcutListener(new ShortcutListener("Commit", ShortcutAction.KeyCode.ENTER, null) {
				private static final long serialVersionUID = 1L;

				@Override
				public void handleAction(Object sender, Object target) {
					if (target == VaadinTextAreaField.this) {
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

	private class VaadinTextFieldTextChangeListener implements TextChangeListener {
		private static final long serialVersionUID = 1L;
		private final InputComponentListener changeListener;
		
		public VaadinTextFieldTextChangeListener(InputComponentListener changeListener) {
			this.changeListener = changeListener;
		}

		@Override
		public void textChange(TextChangeEvent event) {
			VaadinTextAreaField.this.event = event;
			changeListener.changed(VaadinTextAreaField.this);
			VaadinTextAreaField.this.event = null;
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
			setValue(text);
			setReadOnly(true);
		} else {
			setValue(text);
		}		
	}

	@Override
	public String getValue() {
		if (event != null) {
			return event.getText();
		} else {
			return (String) getValue();
		}
	}
	
}
