package ch.openech.mj.page;

import java.util.List;

import ch.openech.mj.application.ApplicationContext;
import ch.openech.mj.toolkit.IComponent;

/**
 * These are the possibilities of a page or an action opening a page<p>
 * 
 * The implementation must be a component of its toolkit because the
 * PageContext is found through the getParent-chain in the actions
 */
public interface PageContext extends IComponent {

	/**
	 * The page wants to closte itself. Meaning: go backward without possibility to go forward
	 * 
	 */
	void closeTab();
	
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
	 * Add a new tab/context
	 * 
	 * @return the new PageContext
	 */
	PageContext addTab();

	/**
	 * 
	 * @return ApplicationContext
	 */
	ApplicationContext getApplicationContext();
	
}
