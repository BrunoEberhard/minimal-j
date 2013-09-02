package ch.openech.mj.page;

import ch.openech.mj.resources.Resources;


public abstract class AbstractPage implements Page {

	private final PageContext pageContext;
	
	protected AbstractPage(PageContext pageContext) {
		this.pageContext = pageContext;
	}

	public PageContext getPageContext() {
		return pageContext;
	}	
	
	@Override
	public String getTitle() {
		return Resources.getString(getClass().getSimpleName() + ".text");
	}

	protected void show(Class<? extends Page> pageClass, String... args) {
		String pageLink = link(pageClass, args);
		pageContext.show(pageLink);
	}

	public static String link(Class<? extends Page> pageClass, String... args) {
		return PageLink.link(pageClass, args);
	}
	
}
