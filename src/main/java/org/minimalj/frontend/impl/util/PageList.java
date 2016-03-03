package org.minimalj.frontend.impl.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.minimalj.frontend.page.Page;

public class PageList {
	private final List<String> pageIds = new ArrayList<>();
	private final List<Page> pages = new ArrayList<>();
	
	/**
	 * @param pageId pageId to add
	 * @param page page to add
	 */
	public void put(String pageId, Page page) {
		Objects.nonNull(pageId);
		Objects.nonNull(page);
		pageIds.add(pageId);
		pages.add(page);
	}

	/**
	 * @param index index
	 * @return id at that position
	 */
	public String getId(int index) {
		return pageIds.get(index);
	}
	
	/**
	 * @param page Page
	 * @return id of page
	 */
	public String getId(Page page) {
		int index = indexOf(page);
		return getId(index);
	}

	/**
	 * @param page Page
	 * @return index of this page or -1 if not in list
	 */
	public int indexOf(Page page) {
		return pages.indexOf(page);
	}

	/**
	 * @param page Page
	 * @return true if page is in this list
	 */
	public boolean contains(Page page) {
		return pages.contains(page);
	}

	/**
	 * removes all entries
	 */
	public void clear() {
		pageIds.clear();
		pages.clear();
	}

	/**
	 * @param pageId this page and all following are removed
	 */
	public void removeAllFrom(String pageId) {
		int pagePos = pageIds.indexOf(pageId);
		if (pagePos < 0) throw new IllegalArgumentException(pageId + " not a visible page");
		removeAllFrom(pagePos);
	}
	
	/**
	 * @param page this page and all following are removed
	 */
	public void removeAllFrom(Page page) {
		int pagePos = pages.indexOf(page);
		if (pagePos < 0) throw new IllegalArgumentException(page + " not a visible page");
		removeAllFrom(pagePos);
	}
	
	private void removeAllFrom(int pagePos) {
		for (int i = pageIds.size()-1; i >= pagePos; i--) {
			pages.remove(i);
			pageIds.remove(i);
		}
	}
}
