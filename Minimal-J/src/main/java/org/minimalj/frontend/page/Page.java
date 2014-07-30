package org.minimalj.frontend.page;

import org.minimalj.frontend.toolkit.ClientToolkit.IContent;


public interface Page {
	
	public String getTitle();
	
	public IContent getContent();

}