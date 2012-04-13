package ch.openech.mj.application;

import java.awt.Component;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import ch.openech.mj.edit.form.FormVisual;
import ch.openech.mj.page.ObjectPage;
import ch.openech.mj.page.Page;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.RefreshablePage;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.util.StringUtils;

@Deprecated
public abstract class HistoryViewPage<T> extends Page implements RefreshablePage, ObjectPage<T> {

	private JSplitPane splitPane;
	private FormVisual<T> objectPanel;
	
	private JScrollPane tableScrollPane;
	private JTable table;
	private HistoryTableModel tableModel;
	
	public HistoryViewPage(PageContext context) {
		super(context);
	}

	protected abstract List<T> loadObjects();

	protected abstract String getTime(T object);

	protected abstract String getDescription(T object);
	
	protected abstract FormVisual<T> createForm();
	
	@Override
	public IComponent getPanel() {
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setDividerLocation(200);
		splitPane.setBorder(null);

		objectPanel = createForm();

		splitPane.setRightComponent((Component) objectPanel.getComponent());
		initializeTable();
		splitPane.setLeftComponent(tableScrollPane);
		
		// TODO !!
		return null;
	}

	private void initializeTable() {
		if (tableScrollPane != null) return;
		
		tableModel = new HistoryTableModel();
		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableScrollPane = new JScrollPane(table);

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;

				int row = table.getSelectedRow();
				if (row >= 0) {
					showObject(tableModel.getHistoryObjects().get(row), row);
				}
			}
		});
		refresh();
		
//		setFirstColumnWidth();
	}

	@Override
	public T getObject() {
		return tableModel.getActualObject();
	}

	protected void showObject(T object, int row) {
		objectPanel.setObject(object);
	}
	
	@Override
	public void refresh() {
		tableModel.setHistoryObjects(loadObjects());
		table.getSelectionModel().setSelectionInterval(0, 0);

	}

	public void selectActual() {
		if (table.getRowCount() > 0) {
			table.getSelectionModel().setSelectionInterval(0, 0);
		}
	}
	
//	private void setFirstColumnWidth() {
//		 TODO
//		 if (table == null || table.getGraphics() == null) return;
//		 int width = StringUtils.pixelWidth("88.88.88 88:88:88", table);
//		 table.getColumnModel().getColumn(0).setMinWidth(width);
//		 table.getColumnModel().getColumn(0).setWidth(width);
//		 table.getColumnModel().getColumn(0).setMaxWidth(width);
//	}
//
//	@Override
//	public void updateUI() {
//		super.updateUI();
//		setFirstColumnWidth();
//	}

	private class HistoryTableModel extends AbstractTableModel {
		private List<T> historyObjects;

		public HistoryTableModel() {
		}

		public void setHistoryObjects(List<T> historyObjects) {
			this.historyObjects = historyObjects;
			fireTableDataChanged();
		}

		public List<T> getHistoryObjects() {
			return historyObjects;
		}
		
		public T getActualObject() {
			if (historyObjects != null && historyObjects.size() > 0) {
				return historyObjects.get(0);
			} else {
				return null;
			}
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return historyObjects != null ? historyObjects.size() : 0;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return getTime(historyObjects.get(rowIndex)); 
			if (columnIndex == 1)
				return getDescription(historyObjects.get(rowIndex));
			return "";
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0)
				return "Datum/Zeit";
			if (column == 1)
				return "Ereignis";
			return null;
		}

		private String formatType(String type) {
			if (StringUtils.isBlank(type))
				return "";
			String key = type.substring(0, 1).toUpperCase() + type.trim().substring(1) + ".text";
			String text = Resources.getString(key);
			return text;
		}

	}
	
}
