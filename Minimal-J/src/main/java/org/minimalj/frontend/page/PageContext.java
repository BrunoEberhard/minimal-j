package org.minimalj.frontend.page;

import java.util.List;

import org.minimalj.application.ApplicationContext;
import org.minimalj.frontend.edit.Editor;
import org.minimalj.frontend.toolkit.IComponent;

/**
 * These are the possibilities of a page or an action opening a page<p>
 * 
 */
public interface PageContext extends IComponent {

	/**
	 * Show a page in the same tab/context and generate a new entry in the history list
	 * 
	 * @param page
	 */
	void show(String pageLink);

	/**
	 * Show a page and provide some more Links for a up/down navigating
	 * 
	 */
	void show(List<String> pageLinks, int index);
	
	/**
	 * Show a page "on a page". This could be a dialog. This is completly different
	 * from show(pageLink) as the page is shown without entry in the history list.
	 * 
	 * @param page
	 */
	void show(Editor<?> editor);
	
	/**
	 * 
	 * @return ApplicationContext
	 */
	ApplicationContext getApplicationContext();
	
}
