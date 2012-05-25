package ch.openech.mj.page;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;


public class PageAction extends AbstractAction {
	
	private final Class<? extends Page> pageClass;
	private final String[] parameter;
	
	public PageAction(Class<? extends Page> pageClass) {
		this(pageClass, new String[0]);
	}

	public PageAction(Class<? extends Page> pageClass, String... parameter) {
		this(pageClass, getBaseName(pageClass), parameter);
	}
	
	protected PageAction(Class<? extends Page> pageClass, String baseName, String... parameter) {
		this.pageClass = pageClass;
		this.parameter = parameter;
		ResourceHelper.initProperties(this, Resources.getResourceBundle(), baseName);
	}
	
	private static String getBaseName(Class<? extends Page> pageClass) {
		return pageClass.getSimpleName();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		PageContext pageContext = PageContextHelper.findContext(e.getSource());
		showPageOn(pageContext);
	}

	private void showPageOn(PageContext context) {
		context.show(Page.link(pageClass, parameter));
	}
	
}
