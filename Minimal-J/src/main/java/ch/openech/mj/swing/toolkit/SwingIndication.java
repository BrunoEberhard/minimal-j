package ch.openech.mj.swing.toolkit;

import java.awt.Component;
import java.awt.Container;
import java.util.List;

import javax.swing.JScrollPane;

import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;

public class SwingIndication {

	public static void setValidationMessages(List<ValidationMessage> validationMessages, Container container) {
		List<ValidationMessage> filteredMessages = ValidationMessage.filterValidationMessage(validationMessages, container.getName());
		setIndicationToComponents(filteredMessages, container);
	}

	private static void setIndicationToComponents(List<ValidationMessage> validationMessages, Container container) {
		for (Component child : container.getComponents()) {
			setIndicationToComponent(validationMessages, child);
		}
	}

	private static void setIndicationToComponent(List<ValidationMessage> validationMessages, Component component) {
		if (component instanceof JScrollPane) {
			component = ((JScrollPane) component).getViewport().getView();
		}
		if (component instanceof Indicator) {
			((Indicator) component).setValidationMessages(validationMessages);
		}
		if (component instanceof Container) {
			Container container = (Container) component;
			setIndicationToComponents(validationMessages, container);
		}
	}

	
}
