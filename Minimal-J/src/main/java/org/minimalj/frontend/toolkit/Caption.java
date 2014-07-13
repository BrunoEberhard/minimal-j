package org.minimalj.frontend.toolkit;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;

public interface Caption {

	public void setValidationMessages(List<String> validationMessages);

	public IComponent getComponent();
	
}
