package ch.openech.mj.application;

import ch.openech.mj.page.Page;
import ch.openech.mj.toolkit.ClientToolkit;

public class EmptyPage extends Page {

	private final Object emptyComponent;
	
	public EmptyPage() {
		super();
		emptyComponent = ClientToolkit.getToolkit().createEmptyComponent();
	}
	
	@Override
	public Object getPanel() {
		return emptyComponent;
	}

}
