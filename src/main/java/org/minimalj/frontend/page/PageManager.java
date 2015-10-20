package org.minimalj.frontend.page;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.security.Subject;

public interface PageManager {
	
	public default Subject getSubject() {
		// TODO remove this default value, every PageManager should somehow implement getSubject()
		return null;
	}

	public default void setSubject(Subject subject) {
		// TODO remove this default
	}
	
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

	public abstract <T> IDialog showSearchDialog(Search<T> index, Object[] keys, TableActionListener<T> listener);

	//
	
	public abstract void showMessage(String text);
	
	public abstract void showError(String text);	
}