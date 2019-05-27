package org.minimalj.frontend.impl.util;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.page.Page;

public class PageStore {

	private final static int HISTORY_LENGTH = Integer.parseInt(Configuration.get("MjHistoryLength", "20"));
	private final ArrayBlockingQueue<PageStoreEntry> queue = new ArrayBlockingQueue<>(HISTORY_LENGTH);
	
	public String put(Page page) {
		PageStoreEntry entry = new PageStoreEntry(page);
		if (queue.remainingCapacity() == 0) {
			queue.poll();
		}
		queue.add(entry);
		return entry.getId();
	}
	
	public Page get(String pageId) {
		Optional<PageStoreEntry> optional = queue.stream().filter(entry -> entry.getId().equals(pageId)).findFirst();
		if (optional.isPresent()) {
			PageStoreEntry entry = optional.get();
			queue.remove(entry);
			queue.add(entry);
			return entry.getPage();
		} else {
			throw new IllegalStateException(pageId);
		}
	}
	
	public boolean valid(List<String> pageIds) {
		return pageIds.stream().allMatch(pageId -> queue.stream().anyMatch(entry -> entry.getId().equals(pageId)));
	}
	
	private static class PageStoreEntry {
		private final String id;
		private final Page page;

		public PageStoreEntry(Page page) {
			this.id = UUID.randomUUID().toString();
			this.page = page;
		}
		
		public Page getPage() {
			return page;
		}

		public String getId() {
			return id;
		}
	}
}
