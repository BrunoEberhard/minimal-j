package ch.openech.mj.page;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.annotation.Size;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.ITable;
import ch.openech.mj.toolkit.ITable.TableActionListener;

public abstract class HistoryPage<T> extends AbstractPage implements RefreshablePage {

	private List<HistoryVersion<T>> versions;
	private ITable<HistoryVersion<T>> table;
	
	public HistoryPage(PageContext pageContext) {
		super(pageContext);
		table = ClientToolkit.getToolkit().createTable(new Object[]{HistoryVersion.HISTORY_VERSION.version, HistoryVersion.HISTORY_VERSION.time, HistoryVersion.HISTORY_VERSION.description});
		table.setClickListener(new TableActionListener<HistoryVersion<T>>() {
			@Override
			public void action(HistoryVersion<T> selectedObject, List<HistoryVersion<T>> selectedObjects) {
				List<String> pageLinks = new ArrayList<String>(versions.size());
				int selectedIndex = 0;
				int count = 0;
				for (HistoryVersion<T> version : versions) {
					if (version == selectedObject) {
						selectedIndex = count;
					}
					count++;
					String link = link(version.object, version.version);
					pageLinks.add(link);
				}
				getPageContext().show(pageLinks, selectedIndex);
			}
		});
	}
	
	@Override
	public ActionGroup getMenu() {
		return null;
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

		public static final HistoryVersion<?> HISTORY_VERSION = Keys.of(HistoryVersion.class);
		
		public String version;
		public LocalDateTime time;
		@Size(255)
		public String description;
		public T object;

	}

}
