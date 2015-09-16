package org.minimalj.frontend.impl.util;

import java.util.HashMap;
import java.util.Map;

import org.minimalj.frontend.page.Page;

public class PageStore {

	private final Map<String, PageStoreEntry> pagesById = new HashMap<>();
	
	public void put(String pageId, Page page) {
		if (pagesById.containsKey(pageId)) {
			PageStoreEntry entry = pagesById.get(pageId);
			entry.updateLastUsed();
		} else {
			PageStoreEntry entry = new PageStoreEntry(page);
			pagesById.put(pageId, entry);
		}
	}
	
	public Page get(String pageId) {
		if (pagesById.containsKey(pageId)) {
			PageStoreEntry entry = pagesById.get(pageId);
			return entry.getPage();
		} else {
			throw new IllegalStateException(pageId);
		}
	}
	
	private static class PageStoreEntry {
		private final Page page;
		private long lastUsed;

		public PageStoreEntry(Page page) {
			this.page = page;
			updateLastUsed();
		}
		
		public void updateLastUsed() {
			lastUsed = System.currentTimeMillis();
		}
		
		public Page getPage() {
			return page;
		}
	}
}
