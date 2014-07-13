package org.minimalj.frontend.page;

import java.util.List;

import org.minimalj.application.ApplicationContext;
import org.minimalj.frontend.toolkit.ClientToolkit.IContext;

/**
 * These are the possibilities of a page or an action opening a page<p>
 * 
 */
// => ClientContext?
public interface PageContext extends IContext {

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
	 * 
	 * @return ApplicationContext
	 */
	ApplicationContext getApplicationContext();
	
}
