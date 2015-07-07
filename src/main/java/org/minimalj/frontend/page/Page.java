package org.minimalj.frontend.page;

import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
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
	
	// TODO rename to getActions, return Array of Actions
	public ActionGroup getMenu() {
		return null;
	}
}