package ch.openech.mj.edit;

import java.util.List;

import javax.swing.Icon;

import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.resources.ResourceAction;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;


public abstract class SaveAction extends ResourceAction implements Indicator {
	private String resourceBase;
	
	public SaveAction(String resourceBase) {
		this.resourceBase = resourceBase;
		ResourceHelper.initProperties(this, Resources.getResourceBundle(), resourceBase);
	}
	
	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		String iconKey;
		String description;
		boolean valid = validationMessages == null || validationMessages.isEmpty();
		if (valid) {
			iconKey = resourceBase + ".icon.Ok";
			description = "Eingaben speichern";
		} else {
			iconKey = resourceBase + ".icon.Error";
			description = ValidationMessage.formatHtml(validationMessages);
		}
		
		Icon icon = ResourceHelper.getIcon(Resources.getResourceBundle(), iconKey);
		putValue(LARGE_ICON_KEY, icon);
		putValue(SMALL_ICON, icon);
		putValue(SHORT_DESCRIPTION, description);
	}

}