package org.minimalj.frontend.vaadin.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ClientToolkit.Input;
import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Component;

/**
 * Nearly identical with TextField but extends from TextArea.
 * Maybe some code could be deduplicated.
 * 
 */
public class VaadinTextAreaField extends com.vaadin.ui.TextArea implements IComponent {
	private static final long serialVersionUID = 1L;

	private TextChangeEvent event;
	private Runnable commitListener;
	
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


//	@Override
//	public void setCommitListener(Runnable commitListener) {
//		this.commitListener = commitListener;
//	}
	
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
	public Object getValue() {
		if (event != null) {
			return event.getText();
		} else {
			return super.getValue();
		}
	}
	
	public static class VaadinTextAreaDelegate implements Input<String>, VaadinDelegateComponent {
		
		private final VaadinTextAreaField delegate;
		
		public VaadinTextAreaDelegate(InputComponentListener changeListener, int maxLength, String allowedCharacters) {
			this.delegate = new VaadinTextAreaField(changeListener, maxLength, allowedCharacters);
		}

		@Override
		public void setValue(String value) {
			delegate.setValue(value);
		}

		@Override
		public String getValue() {
			return (String) delegate.getValue();
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

//	public void setValue(Object value) {
//		setValue((String) value);
//	}
//	
//	public String getValue() {
//		return (String) getValue();
//	}

}
