package org.minimalj.frontend.page;

import java.util.List;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.util.resources.Resources;

/**
 * Pages are one of the building blocks of an application. They are intended to fill the whole space
 * of a window (or simply the display space if the frontend has no windows). Their content is static
 * in contrast to dialogs which are meant to allow inputs.<p>
 * 
 * A page can have a title and a content. Pages are meant to be ContentProvider. Pages are
 * allocated a lot. They should be lightweight! The heavy stuff should be done when calling
 * getContent(). 
 *
 */
public abstract class Page {
	
	public String getTitle() {
		return Resources.getString(getClass());
	}
	
	public abstract IContent getContent();
	
	public List<Action> getActions() {
		return null;
	}
	
	/**
	 * Allows the Frontend to make a bookmark or a link for this Page. Note that
	 * the user may see this String (for example in the URL). The parts of the
	 * route should be glued together with '/'. For example "person/42".
	 * Although there is no explicit limit to the length of the route it should
	 * stay human readable.
	 * 
	 * @see org.minimalj.application.Application#createPage(String)
	 * @return <code>null</code> if this Page class or object is not routable.
	 */
	public String getRoute() {
		return null;
	}
}