package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend.HasCaption;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;


/**
 * 
 * @author Bruno
 *
 */
public class VaadinTextField extends TextField implements Input<String>, HasCaption {
	private static final long serialVersionUID = 1L;

	public VaadinTextField(InputComponentListener changeListener, int maxLength) {
		this(changeListener, maxLength, null);
	}
	
	public VaadinTextField(InputComponentListener changeListener, int maxLength, String allowedCharacters) {
		setMaxLength(maxLength);
		if (changeListener != null) {
			addValueChangeListener(event -> changeListener.changed(VaadinTextField.this));
			setValueChangeMode(ValueChangeMode.TIMEOUT);
	//			addShortcutListener(new CommitShortcutListener());
		} else {
			setReadOnly(true);
		}
	}
	
//	static class CommitShortcutListener extends ShortcutListener {
//		private static final long serialVersionUID = 1L;
//
//		public CommitShortcutListener() {
//			super("Commit", ShortcutAction.KeyCode.ENTER, null);
//		}
//		
//		@Override
//		public void handleAction(Object sender, Object target) {
//			if (target instanceof AbstractTextField) {
//				VaadinDialog dialog = findDialog((AbstractTextField) target);
//				if (dialog != null) {
//					Action saveAction = dialog.getSaveAction();
//					if (saveAction.isEnabled()) {
//						saveAction.action();
//					}						
//				}
//			}
//		}
//	}
//
//	static VaadinDialog findDialog(Component c) {
//		while (c != null) {
//			if (c instanceof VaadinDialog) {
//				return (VaadinDialog) c;
//			}
//			c = c.getParent();
//		}
//		return null;
//	}

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