package org.minimalj.frontend.vaadin.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;
import org.minimalj.frontend.toolkit.TextField;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Component;

/**
 * 
 * @author Bruno
 *
 */
public class VaadinTextField extends com.vaadin.ui.TextField implements IComponent {
	private static final long serialVersionUID = 1L;

	private TextChangeEvent event;
	private Runnable commitListener;
	
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
	public void setValue(Object text) {
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
			return (String) super.getValue();
		}
	}

	public static class VaadinTextDelegate implements TextField, VaadinDelegateComponent {
		
		private final VaadinTextField delegate;
		
		public VaadinTextDelegate(InputComponentListener changeListener, int maxLength, String allowedCharacters) {
			this.delegate = new VaadinTextField(changeListener, maxLength, allowedCharacters);
		}

		@Override
		public void setValue(String value) {
			delegate.setValue(value);
		}

		@Override
		public String getValue() {
			return delegate.getValue();
		}

		@Override
		public void setEditable(boolean editable) {
			delegate.setReadOnly(!editable);
		}

		@Override
		public void setCommitListener(Runnable runnable) {
			// TODO Auto-generated method stub
		}

		@Override
		public Component getDelegate() {
			return delegate;
		}
	}

	
}
