package ch.openech.mj.page;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;


public class PageAction extends AbstractAction {
	public static final String PAGE = "Page";
	
	private final PageContext context;
	private final Class<? extends Page> pageClass;
	private final String parameter;
	
	public PageAction(PageContext context, Class<? extends Page> pageClass) {
		this(context, pageClass, null);
	}
	
	public PageAction(PageContext context, Class<? extends Page> pageClass, String parameter) {
		this(context, pageClass, parameter, getBaseName(pageClass));
	}

	private static String getBaseName(Class<? extends Page> pageClass) {
		String baseName = pageClass.getSimpleName();
		if (baseName.endsWith(PAGE)) baseName = baseName.substring(0, baseName.length() - PAGE.length());
		return baseName;
	}
	
	public PageAction(PageContext context, Class<? extends Page> pageClass, String parameter, String baseName) {
		ResourceHelper.initProperties(this, Resources.getResourceBundle(), baseName);
		this.context = context;
		this.pageClass = pageClass;
		this.parameter = parameter;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		showPageOn(context.addTab());
	}

	protected void showPageOn(PageContext context) {
		context.show(Page.link(pageClass, parameter));
	}
	
}
