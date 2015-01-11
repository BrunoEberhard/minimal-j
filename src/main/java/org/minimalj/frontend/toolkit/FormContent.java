package org.minimalj.frontend.toolkit;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;


public interface FormContent extends IContent {
	
	public void add(IComponent component);

	public void add(String caption, IComponent component, int span);

	public void setValidationMessages(IComponent component, List<String> validationMessages);


}
