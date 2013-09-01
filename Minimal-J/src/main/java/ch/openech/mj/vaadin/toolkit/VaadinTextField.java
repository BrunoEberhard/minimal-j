package ch.openech.mj.vaadin.toolkit;

import java.awt.event.FocusListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.toolkit.TextField;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.HorizontalLayout;

/**
 * A Vaadin Widget must not implement strange interfaces (meaning our TextField) because
 * it would not be recognized by the gwt compiler. So this VaadinTextField cannot extend
 * the vaadin.TextField and implement TextField directly. Everything is delegated.
 * 
 * @author Bruno
 *
 */
public class VaadinTextField extends HorizontalLayout implements TextField {

	private com.vaadin.ui.TextField textWidget;

	public VaadinTextField() {
		this(null, new com.vaadin.ui.TextField());
	}
	
	public VaadinTextField(ChangeListener changeListener, int maxLength) {
		this(changeListener, new com.vaadin.ui.TextField());
		textWidget.setMaxLength(maxLength);
	}
	
	public VaadinTextField(ChangeListener changeListener, int maxLength, String allowedCharacters) {
		this(changeListener, new com.vaadin.ui.TextField());
		textWidget.setMaxLength(maxLength);
	}
	
	private VaadinTextField(ChangeListener changeListener, com.vaadin.ui.TextField vaadinTextWidget) {
		textWidget = vaadinTextWidget;
		textWidget.setNullRepresentation("");
		textWidget.setImmediate(true);
		if (changeListener != null) {
			textWidget.addListener(new VaadinTextFieldTextChangeListener(changeListener));
		} else {
			textWidget.setReadOnly(true);
		}
		addComponent(textWidget);
		textWidget.setSizeFull();
	}

	@Override
	public void setText(String text) {
		textWidget.setValue(text);
	}

	@Override
	public String getText() {
		return (String) textWidget.getValue();
	}

	@Override
	public void setFocusListener(FocusListener focusListener) {
		// TODO
	}

	@Override
	public void setCommitListener(Runnable runnable) {
		// TODO listening to Enter Key at Vaadin TextField
	}
	
	private class VaadinTextFieldTextChangeListener implements TextChangeListener {
		private final ChangeListener changeListener;
		
		public VaadinTextFieldTextChangeListener(ChangeListener changeListener) {
			this.changeListener = changeListener;
		}

		@Override
		public void textChange(TextChangeEvent event) {
			changeListener.stateChanged(new ChangeEvent(VaadinTextField.this));
		}
	}
	
}
