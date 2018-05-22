package org.minimalj.frontend.impl.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.minimalj.frontend.page.Page;

public class PageStore {

	private final Map<String, PageStoreEntry> pagesById = new HashMap<>();
	
	public String put(Page page) {
		String pageId = UUID.randomUUID().toString();
		PageStoreEntry entry = new PageStoreEntry(page);
		pagesById.put(pageId, entry);
		return pageId;
	}
	
	public Page get(String pageId) {
		if (pagesById.containsKey(pageId)) {
			PageStoreEntry entry = pagesById.get(pageId);
			return entry.getPage();
		} else {
			throw new IllegalStateException(pageId);
		}
	}
	
	public boolean valid(List<String> pageIds) {
		return pageIds.stream().allMatch(pagesById::containsKey);
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
