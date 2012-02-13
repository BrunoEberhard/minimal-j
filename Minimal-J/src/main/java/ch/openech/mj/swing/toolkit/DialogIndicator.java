package ch.openech.mj.swing.toolkit;

import java.util.List;

import javax.swing.JOptionPane;

import ch.openech.mj.application.EditablePanel;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.resources.Resources;

public class DialogIndicator implements Indicator {

	private final String resourceBase;
	
	public DialogIndicator(String resourceBase) {
		this.resourceBase = resourceBase;
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		StringBuilder s = new StringBuilder();
		for (ValidationMessage validationMessage : validationMessages) {
			if (validationMessage.getKey() != null) {
				s.append(Resources.getString(resourceBase + "." + validationMessage.getKey()));
				s.append(": ");
			}
			s.append(validationMessage.getFormattedText());
			s.append("\n");
		}
		
		// TODO Option auf internal wieder einbauen
		// EditablePanel.getEditablePanel((Component) editable);
		EditablePanel editablePanel = null; 
		if (editablePanel != null) {
			JOptionPane.showInternalMessageDialog(null, s.toString(), "Eingaben unvollständig oder nicht gültig", JOptionPane.ERROR_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, s.toString(), "Eingaben unvollständig oder nicht gültig", JOptionPane.ERROR_MESSAGE);
		}
	}
	
}
