package ch.openech.mj.swing.toolkit;

import java.awt.Component;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.resources.ResourceHelper;

public class SwingIndication {

	public static void setValidationMessagesToCaption(List<ValidationMessage> validationMessages, Component component) {
		if (component.getParent() instanceof JPanel) {
			JPanel panel = (JPanel) component.getParent();
			if (panel.getComponent(0) instanceof JLabel) {
				JLabel captionLabel = (JLabel)panel.getComponent(0);
				if (!validationMessages.isEmpty()) {
					captionLabel.setIcon(ResourceHelper.getIcon("field_error.png"));
					String validationMessage = ValidationMessage.formatHtml(validationMessages);
					captionLabel.setToolTipText(validationMessage);
				} else {
					captionLabel.setIcon(null);
					captionLabel.setToolTipText(null);
				}
			}
		}
	}

	
}
