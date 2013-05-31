package ch.openech.mj.vaadin.toolkit;

import java.awt.event.ActionListener;
import java.awt.event.FocusListener;

import javax.swing.event.ChangeListener;

import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.vaadin.widgetset.VaadinTextWidget;

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

	private VaadinTextWidget textWidget;

	public VaadinTextField() {
		this(null, new VaadinTextWidget());
	}
	
	public VaadinTextField(ChangeListener changeListener, int maxLength) {
		this(changeListener, new VaadinTextWidget());
		textWidget.setMaxLength(maxLength);
	}
	
	public VaadinTextField(ChangeListener changeListener, int maxLength, String allowedCharacters) {
		this(changeListener, new VaadinTextWidget(allowedCharacters));
		textWidget.setMaxLength(maxLength);
	}
	
	private VaadinTextField(ChangeListener changeListener, VaadinTextWidget vaadinTextWidget) {
		textWidget = vaadinTextWidget;
		textWidget.setNullRepresentation("");
		textWidget.setImmediate(true);
		if (changeListener != null) {
			textWidget.setChangeListener(changeListener);
		} else {
			textWidget.setEditable(false);
		}
		addComponent(textWidget);
		textWidget.setSizeFull();
	}

	@Override
	public void setText(String text) {
		textWidget.setText(text);
	}

	@Override
	public String getText() {
		return textWidget.getText();
	}

	@Override
	public void setFocusListener(FocusListener focusListener) {
		textWidget.setFocusListener(focusListener);
	}

	@Override
	public void setCommitListener(ActionListener listener) {
		// TODO listening to Enter Key at Vaadin TextField
	}
	
}
