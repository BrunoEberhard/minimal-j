package ch.openech.mj.page;

import java.util.ArrayList;
import java.util.List;
import org.joda.time.LocalDateTime;

import ch.openech.mj.model.Constants;
import ch.openech.mj.model.annotation.Size;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.VisualTable;
import ch.openech.mj.toolkit.VisualTable.ClickListener;

public abstract class HistoryPage<T> extends Page implements RefreshablePage {

	private List<HistoryVersion<T>> versions;
	private VisualTable<HistoryVersion<T>> table;
	
	public HistoryPage(PageContext context) {
		super(context);
		VisualTable<?> table2 = ClientToolkit.getToolkit().createVisualTable(HistoryVersion.class, new Object[]{HistoryVersion.HISTORY_VERSION.time, HistoryVersion.HISTORY_VERSION.description});
		table = (VisualTable<HistoryVersion<T>>) table2;
		table.setClickListener(new ClickListener() {
			@Override
			public void clicked() {
				int index = table.getSelectedIndex();
				if (index >= 0) {
					List<String> pageLinks = new ArrayList<String>(versions.size());
					for (HistoryVersion<T> version : versions) {
						String link = link( version.object, version.version);
						pageLinks.add(link);
					}
					getPageContext().show(pageLinks, index);
				}
			}
		});
	}
	
	protected abstract List<HistoryVersion<T>> loadVersions();

	protected abstract LocalDateTime getTime(T object);

	protected abstract String getDescription(T object);
	
	protected abstract String link(T object, String version);

	@Override
	public IComponent getComponent() {
		if (versions == null) {
			refresh();
		}
		return table;
	}

	@Override
	public void refresh() {
		versions = loadVersions();
		table.setObjects(versions);
	}

	public static class HistoryVersion<T> {

		public static final HistoryVersion<?> HISTORY_VERSION = Constants.of(HistoryVersion.class);
		
		public String version;
		public LocalDateTime time;
		@Size(255)
		public String description;
		public T object;
	}

}
