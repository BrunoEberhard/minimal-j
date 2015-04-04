package org.minimalj.frontend.page;

import org.minimalj.frontend.toolkit.ClientToolkit.IContent;

public class EmptyPage implements Page {

	public EmptyPage() {
	}
	
	@Override
	public IContent getContent() {
		return null;
	}

}
