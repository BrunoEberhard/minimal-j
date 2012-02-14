package ch.openech.mj.vaadin.widgetset;

import java.awt.event.FocusListener;
import java.util.List;
import java.util.Map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField.TextFieldFilter;
import ch.openech.mj.vaadin.toolkit.VaadinComponentDelegate;
import ch.openech.mj.vaadin.toolkit.VaadinIndication;
import ch.openech.mj.vaadin.widgetset.client.ui.VVaadinTextField;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;


@com.vaadin.ui.ClientWidget(ch.openech.mj.vaadin.widgetset.client.ui.VVaadinTextField.class)
public class VaadinTextWidget extends com.vaadin.ui.TextField {

	private final TextFieldFilter filter;
	private final IComponent vaadinComponentDelegate;
	private TextFieldChangeListener changeListener;
	
	public VaadinTextWidget() {
		this(null);
	}
	
	public VaadinTextWidget(TextFieldFilter filter) {
		setReadOnly(false);
		this.filter = filter;
		this.vaadinComponentDelegate = new VaadinComponentDelegate(this);
	}
	
//	@Override
	public void requestFocus() {
		super.focus();
	}

//	@Override
	public void setText(String text) {
		if (isReadOnly()) {
			setReadOnly(false);
			super.setValue(text);
			setReadOnly(true);
		} else {
			super.setValue(text);
		}
	}

//	@Override
	public String getText() {
		return (String) super.getValue();
	}

//	@Override
	public void setChangeListener(ChangeListener listener) {
		if (changeListener == null) {
			changeListener = new TextFieldChangeListener();
		}
		changeListener.setChangeListener(listener);
	}
	
	public class TextFieldChangeListener implements ValueChangeListener {

		private ChangeListener changeListener;
		
		public void setChangeListener(ChangeListener changeListener) {
			if (changeListener == null) {
				if (this.changeListener != null) {
					removeListener(this);
				}
			} else {
				if (this.changeListener == null) {
					addListener(this);
				}
			}		
			this.changeListener = changeListener;
		}
		
		@Override
		public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
			changeListener.stateChanged(new ChangeEvent(VaadinTextWidget.this));
		}
	}
	
//	@Override
	public void setEditable(boolean editable) {
		super.setReadOnly(!editable);
	}

	@Override
	public void setEnabled(boolean editable) {
		super.setEnabled(editable);
	}

//	@Override
	public void setFocusListener(FocusListener focusListener) {
		// TODO Adapter Vaadin -> Swing
		// addListener(listener)
	}

//	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		VaadinIndication.setValidationMessages(validationMessages, this);
	}

	private String response;
	
	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);
		String requested = (String)variables.get(VVaadinTextField.TEXT_REQUEST);
		if (requested != null) {
			if (filter != null) {
				response = filter.filter(vaadinComponentDelegate, requested);
				requestRepaint();
			}
		}
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

		if (response != null) {
			target.addAttribute(VVaadinTextField.TEXT_RESPONSE, response);
		}
	}
	
}
