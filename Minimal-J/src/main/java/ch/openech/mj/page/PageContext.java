package ch.openech.mj.page;

/**
 * These are the possibilities of a page or an action opening a page
 * 
 */
public interface PageContext {

	/**
	 * The page wants to closte itself. Meaning: go backward without possibility to go forward
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
	 * @return The component used as parent for Dialogs
	 */
	Object getComponent();

}
