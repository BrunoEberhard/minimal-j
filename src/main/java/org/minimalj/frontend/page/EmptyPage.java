package org.minimalj.frontend.page;

import org.minimalj.frontend.Frontend.IContent;

public class EmptyPage implements Page {

	public EmptyPage() {
	}
	
	@Override
	public IContent getContent() {
		return null;
	}

}
