package ch.openech.mj.page;

import java.util.prefs.Preferences;



/**
 * These are the possibilities of a page or an action opening a page
 * 
 */
public interface PageContext {

	/**
	 * Close the visible Page. Meaning: go backward without possibility to go forward
	 * 
	 */
	void close();
	
	/**
	 * Show a page in the same tab/context and generate a new entry in the history list
	 * 
	 * @param page
	 */
	void show(String pageLink);

	/**
	 * Add a new tab/context
	 * 
	 * @return the new PageContext
	 */
	PageContext addTab();
	
	/**
	 * 
	 * @return the actual visible Page in this context
	 */
	Page getVisiblePage();
	
	/**
	 * 
	 * @return the users preferences in this context / window
	 */
	Preferences getPreferences();
	
}
