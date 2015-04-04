package org.minimalj.frontend.page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.ClientToolkit.TableActionListener;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public abstract class HistoryPage<T> implements Page {

	public HistoryPage() {
	}

	protected abstract List<HistoryVersion<T>> loadVersions();

	protected abstract LocalDateTime getTime(T object);

	protected abstract String getDescription(T object);
	
	protected abstract Page click(T object, String version);

	@Override
	public IContent getContent() {
		List<HistoryVersion<T>> versions = loadVersions();
		TableActionListener<HistoryVersion<T>> listener = new TableActionListener<HistoryVersion<T>>() {
			@Override
			public void action(HistoryVersion<T> selectedObject, List<HistoryVersion<T>> selectedObjects) {
				List<Page> pages = new ArrayList<>(versions.size());
				int selectedIndex = 0;
				int count = 0;
				for (HistoryVersion<T> version : versions) {
					if (version == selectedObject) {
						selectedIndex = count;
					}
					count++;
					Page page = HistoryPage.this.click(version.object, version.version);
					pages.add(page);
				}
				ClientToolkit.getToolkit().show(pages, selectedIndex);
			}
		};
		IContent table = ClientToolkit.getToolkit().createTable(new Object[]{HistoryVersion.$.version, HistoryVersion.$.time, HistoryVersion.$.description}, listener);
		return table;
	}

	public static class HistoryVersion<T> {
		public static final HistoryVersion<?> $ = Keys.of(HistoryVersion.class);
		
		public String version;
		public LocalDateTime time;
		@Size(255)
		public String description;
		public T object;

	}

}
