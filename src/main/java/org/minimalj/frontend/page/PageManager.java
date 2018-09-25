package org.minimalj.frontend.page;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;

public interface PageManager {
	
	public abstract void show(Page page);

	public default void showDetail(Page mainPage, Page detail) {
		show(detail);
	}
	
	public default void hideDetail(Page page) {
		// do nothing
	}
	
	public default boolean isDetailShown(Page page) {
		return false;
	}

	//

	public abstract IDialog showDialog(String title, IContent content, Action saveAction, Action closeAction, Action... actions);

	//
	
	public abstract void showMessage(String text);
	
	public abstract void showError(String text);	
}