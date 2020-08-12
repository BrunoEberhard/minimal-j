package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend.HasCaption;

import com.vaadin.flow.component.textfield.TextArea;

/**
 * Nearly identical with TextField but extends from TextArea.
 * Maybe some code could be deduplicated.
 * 
 */
public class VaadinTextAreaField extends TextArea implements Input<String>, HasCaption {
	private static final long serialVersionUID = 1L;

	private Runnable commitListener;
	
	public VaadinTextAreaField(InputComponentListener changeListener, int maxLength) {
		this(changeListener, maxLength, null);
	}
	
	public VaadinTextAreaField(InputComponentListener changeListener, int maxLength, String allowedCharacters) {
		setMaxLength(maxLength);
		if (changeListener != null) {
			addValueChangeListener(event -> changeListener.changed(VaadinTextAreaField.this));
//			addShortcutListener(new ShortcutListener("Commit", ShortcutAction.KeyCode.ENTER, null) {
//				private static final long serialVersionUID = 1L;
//
//				@Override
//				public void handleAction(Object sender, Object target) {
//					if (target == VaadinTextAreaField.this) {
//						if (commitListener != null) {
//							commitListener.run();
//						}
//					}
//				}
//			});
		} else {
			setReadOnly(true);
		}
	}

	@Override
	public void setEditable(boolean editable) {
		setReadOnly(!editable);
	}

	@Override
	public void setValue(String text) {
		if (text == null) {
			clear();
			return;
		}
		super.setValue(text);
	}
}