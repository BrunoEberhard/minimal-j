package org.minimalj.frontend.page;

import org.minimalj.frontend.toolkit.ClientToolkit.IContent;

/**
 * Pages are one of the building blocks of an application. They are intended to fill the whole space
 * of a window (or simply the display space if the frontend has no windows). Their content is static
 * in contrast to dialogs which are meant to allow inputs.<p>
 * 
 * A page can have a title and a content. There is no way the content or the title can
 * change without a refresh.<p>
 * 
 * The refresh method exists because for some frontends its much cheaper to simple
 * change the data of the page than to create it again from scratch.
 *
 */
public interface Page {
	
	public String getTitle();
	
	public IContent getContent();

	public void refresh();
	
}