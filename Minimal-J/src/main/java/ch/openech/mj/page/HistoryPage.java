package ch.openech.mj.page;

import java.util.ArrayList;
import java.util.List;

import ch.openech.mj.db.model.annotation.Date;
import ch.openech.mj.db.model.annotation.Varchar;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.VisualTable;
import ch.openech.mj.toolkit.VisualTable.ClickListener;

public abstract class HistoryPage<T> extends Page implements RefreshablePage {

	private List<HistoryVersion> versions;
	private VisualTable<HistoryVersion> table;
	
	public HistoryPage(PageContext context) {
		super(context);
		VisualTable<?> table2 = ClientToolkit.getToolkit().createVisualTable(HistoryVersion.class, new Object[]{"time", "description"});
		table = (VisualTable<HistoryVersion>) table2;
		table.setClickListener(new ClickListener() {
			@Override
			public void clicked() {
				int index = table.getSelectedIndex();
				if (index >= 0) {
					List<String> pageLinks = new ArrayList<String>(versions.size());
					for (HistoryVersion version : versions) {
						String link = link(version.object, version.version);
						pageLinks.add(link);
					}
					getPageContext().show(pageLinks, index);
				}
			}
		});
	}
	
	protected abstract List<HistoryVersion> loadVersions();

	protected abstract String getTime(T object);

	protected abstract String getDescription(T object);
	
	protected abstract String link(T object, String version);

	@Override
	public IComponent getPanel() {
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

	public class HistoryVersion {
		
		public String version;
		@Date
		public String time;
		@Varchar(200)
		public String description;
		public T object;
	}

}
