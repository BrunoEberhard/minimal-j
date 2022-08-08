package org.minimalj.frontend.page;

import org.minimalj.frontend.page.Page.Dialog;
import org.minimalj.security.Subject;

public interface PageManager {
	
	public abstract void show(Page page);

	public default void showDetail(Page mainPage, Page detail, boolean horizontalDetailLayout) {
		showDetail(mainPage, detail);
	}

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

	public abstract void showDialog(Dialog dialog);

	public abstract void closeDialog(Dialog dialog);

	//
	
	public abstract void showMessage(String text);
	
	public abstract void showError(String text);

	public abstract void login(Subject subject);

}