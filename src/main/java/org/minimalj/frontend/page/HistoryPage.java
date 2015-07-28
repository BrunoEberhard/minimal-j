package org.minimalj.frontend.page;

import java.time.LocalDateTime;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public abstract class HistoryPage<T> extends Page {

	private transient ITable<HistoryVersion<T>> table;
	
	public HistoryPage() {
	}

	protected abstract List<HistoryVersion<T>> loadVersions();

	protected abstract LocalDateTime getTime(T object);

	protected abstract String getDescription(T object);
	
	protected abstract Page click(T object, String version);

	@Override
	public IContent getContent() {
		if (table == null) {
			TableActionListener<HistoryVersion<T>> listener = new TableActionListener<HistoryVersion<T>>() {
				@Override
				public void action(HistoryVersion<T> selectedObject) {
					Page page = HistoryPage.this.click(selectedObject.object, selectedObject.version);
					Frontend.getBrowser().show(page);
				}
			};
			table = Frontend.getInstance().createTable(new Object[]{HistoryVersion.$.version, HistoryVersion.$.time, HistoryVersion.$.description}, listener);
			refresh();
		}
		return table;
	}

	public void refresh() {
		if (table != null) {
			table.setObjects(loadVersions());
		}
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
