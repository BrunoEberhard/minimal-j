package ch.openech.mj.application;

import ch.openech.mj.page.Page;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;

public class EmptyPage extends Page {

	private final IComponent emptyComponent;
	
	public EmptyPage(PageContext context) {
		super(context);
		emptyComponent = ClientToolkit.getToolkit().createEmptyComponent();
	}
	
	@Override
	public IComponent getPanel() {
		return emptyComponent;
	}

}
