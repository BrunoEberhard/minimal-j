package org.minimalj.frontend.impl.vaadin6.toolkit;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.InputComponentListener;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.PasswordField;

public class VaadinPasswordField extends PasswordField implements IComponent {
	private static final long serialVersionUID = 1L;

	private TextChangeEvent event;
	private Runnable commitListener;
	
	public VaadinPasswordField(InputComponentListener changeListener, int maxLength) {
		setMaxLength(maxLength);
		setNullRepresentation("");
		setImmediate(true);
		if (changeListener != null) {
			addListener(new VaadinPasswordFieldTextChangeListener(changeListener));
			addShortcutListener(new ShortcutListener("Commit", ShortcutAction.KeyCode.ENTER, null) {
				private static final long serialVersionUID = 1L;

				@Override
				public void handleAction(Object sender, Object target) {
					if (target == VaadinPasswordField.this) {
						if (commitListener != null) {
							commitListener.run();
						}
					}
				}
			});
		} else {
			throw new NullPointerException(this.getClass().getSimpleName() +" must have a listener");
		}
	}
	
	public void setCommitListener(Runnable commitListener) {
		this.commitListener = commitListener;
	}
	
	private class VaadinPasswordFieldTextChangeListener implements TextChangeListener {
		private static final long serialVersionUID = 1L;
		private final InputComponentListener changeListener;
		
		public VaadinPasswordFieldTextChangeListener(InputComponentListener changeListener) {
			this.changeListener = changeListener;
		}

		@Override
		public void textChange(TextChangeEvent event) {
			VaadinPasswordField.this.event = event;
			changeListener.changed(VaadinPasswordField.this);
			VaadinPasswordField.this.event = null;
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

	public static class VaadinPasswordDelegate implements org.minimalj.frontend.Frontend.PasswordField, VaadinDelegateComponent {
		
		private final VaadinPasswordField delegate;
		
		public VaadinPasswordDelegate(InputComponentListener changeListener, int maxLength) {
			this.delegate = new VaadinPasswordField(changeListener, maxLength);
		}

		@Override
		public void setValue(char[] value) {
			String text = value != null ? new String(value) : null;
			delegate.setValue(text);
		}

		@Override
		public char[] getValue() {
			String text = delegate.getValue();
			return text != null ? text.toCharArray() : null;
		}

		@Override
		public void setEditable(boolean editable) {
			delegate.setReadOnly(!editable);
		}

		@Override
		public Component getDelegate() {
			return delegate;
		}
	}
}
