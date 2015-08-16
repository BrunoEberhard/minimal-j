package org.minimalj.frontend.page;

import java.io.InputStream;
import java.io.OutputStream;

import org.minimalj.application.Subject;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;

public interface PageBrowser {
	
	public default Subject getSubject() {
		// TODO remove this default value, every PageBrowser should somehow implement getSubject()
		return null;
	}
	
	public abstract void show(Page page);

	public default void showDetail(Page page) {
		show(page);
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
	
	// Up / Dowload
	
	/**
	 * Store the output of a stream locally on a place to select
	 * 
	 * @param buttonText the text displayed probably in a file browser
	 * @return the stream provided through which the local resource can be filled
	 */
	public abstract OutputStream store(String buttonText);

	/**
	 * Select a stream from a locally source
	 * 
	 * @param buttonText the text displayed probably in a file browser
	 * @return the stream provided by the selected local source
	 */
	public abstract InputStream load(String buttonText);
}