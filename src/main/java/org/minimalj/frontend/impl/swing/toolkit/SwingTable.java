package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterEvent.Type;
import javax.swing.event.RowSorterListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.impl.swing.component.SwingDecoration;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.Sortable;
import org.minimalj.util.resources.Resources;

public class SwingTable<T> extends JScrollPane implements ITable<T> {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SwingTable.class.getName());
	
	private final Object[] keys;
	private final List<PropertyInterface> properties;
	private final JTable table;
	private final ItemTableModel tableModel;
	private final TableActionListener<T> listener;
	private final JButton nextButton, prevButton;
	
	private List<T> list;
	private int offset;
	
	public SwingTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		this.keys = keys;
		this.listener = listener;
		
		this.properties = convert(keys);
		
		tableModel = new ItemTableModel();
		table = new JTable(tableModel);

		table.setSelectionMode(multiSelect ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setFillsViewportHeight(true);
		
		setBorder(BorderFactory.createEmptyBorder());
		
//		setDefaultRenderer(BooleanFormat.class, new BooleanTableCellRenderer());
		table.setDefaultRenderer(Object.class, new RenderingTableCellRenderer());
		
		table.setAutoCreateRowSorter(true);
		
		setViewportView(table);
		
		bindRowHeightToFont();

		table.addMouseListener(new SwingTableMouseListener());
		table.getSelectionModel().addListSelectionListener(new SwingTableSelectionListener());
        table.getRowSorter().addRowSorterListener(new SwingTableRowSortingListener());
        
        table.getTableHeader().setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new FlowLayout());
        panel.setOpaque(false);
        prevButton = SwingDecoration.createDecorationButton(SwingDecoration.Part.PREV);
        prevButton.addActionListener(e -> { offset -= 50; setObjects(list); });
		panel.add(prevButton);
		nextButton = SwingDecoration.createDecorationButton(SwingDecoration.Part.NEXT);
		nextButton.addActionListener(e -> { offset += 50; setObjects(list); });
		panel.add(nextButton);
        table.getTableHeader().add(panel, BorderLayout.LINE_END);
	}
	
	private List<PropertyInterface> convert(Object[] keys) {
		List<PropertyInterface> properties = new ArrayList<PropertyInterface>(keys.length);
		for (Object key : keys) {
			PropertyInterface property = Keys.getProperty(key);
			if (property != null) {
				properties.add(property);
			} else {
				logger.log(Level.WARNING, "Key not a property: " + key);
			}
		}
		if (properties.size() == 0) {
			logger.log(Level.SEVERE, "table without valid keys");
		}
		return properties;
	}
	
	private void bindRowHeightToFont() {
		PropertyChangeListener listener = event -> table.setRowHeight(table.getFont().getSize() * 5 / 3 + 2);
		listener.propertyChange(null);
		table.addPropertyChangeListener("UI", listener);
	}

	@Override
	public void setObjects(List<T> list) {
		this.list = list;
		tableModel.setObjects(list.subList(offset, Math.min(list.size(), offset + 50)));
		nextButton.setVisible(list.size() > offset + 50);
		prevButton.setVisible(offset > 0);
	}

	public List<T> getSelectedObjects() {
		List<T> selectedIds = new ArrayList<>(table.getSelectedRowCount());
		for (int row : table.getSelectedRows()) {
			int rowInModel = table.convertRowIndexToModel(row);
			selectedIds.add(tableModel.getObject(rowInModel));
		}
		return selectedIds;
	}

	private class SwingTableMouseListener extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() >= 2 && listener != null) {
		        int row = table.rowAtPoint(e.getPoint());
		        if (e.getClickCount() == 2 && row >= 0) {
		        	int rowInModel = table.convertRowIndexToModel(row);
		        	SwingFrontend.runWithContext(() -> listener.action(tableModel.getObject(rowInModel)));
		        }
			}
		}
	}

	private class SwingTableSelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting() && table.isShowing()) {
				SwingFrontend.runWithContext(() -> listener.selectionChanged(getSelectedObjects()));
			}
		}
	}
	
	private class SwingTableRowSortingListener implements RowSorterListener {
        @Override
        public void sorterChanged(RowSorterEvent e) {
        	if (e.getType() == Type.SORT_ORDER_CHANGED) {
        		@SuppressWarnings("unchecked")
				List<SortKey> sortKeys = e.getSource().getSortKeys();
        		Object[] keys = new Object[sortKeys.size()];
        		boolean[] directions = new boolean[sortKeys.size()];
        		int index = 0;
        		for (SortKey s : sortKeys) {
        			keys[index] = SwingTable.this.keys[s.getColumn()];
        			directions[index] = s.getSortOrder() == SortOrder.ASCENDING;
        			index++;
        		}
        		if (list instanceof Sortable) {
        			((Sortable) list).sort(keys, directions);
        		}
        		tableModel.setObjects(list.subList(offset, Math.min(list.size(), offset + 50)));
        	}
        }
    }
    
	public class ItemTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private List<T> objects = Collections.emptyList();
		
		public ItemTableModel() {
		}

		public void setObjects(List<T> objects) {
			this.objects = objects;
			fireTableDataChanged();
		}
		
		public List<T> getObjects() {
			return objects;
		}
		
		public T getObject(int index) {
			return objects.get(index);
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public String getColumnName(int column) {
			PropertyInterface property = properties.get(column)
;			return Resources.getPropertyName(property);
		}

		@Override
		public Object getValueAt(int row, int column) {
			try {
				Object object = getObject(row);
				PropertyInterface property = properties.get(column);
				return property.getValue(object);
			} catch (Exception x) {
				logger.severe("Couldn't get value for " + row + "/" + column + ": " + x.getMessage());
				return row + "/" + column + ": " + x.getMessage();
			}
		}

		@Override
		public int getRowCount() {
			return objects.size();
		}

		@Override
		public int getColumnCount() {
			return keys.length;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> clazz = properties.get(columnIndex).getClazz();
			if (Rendering.class.isAssignableFrom(clazz)) {
				return Rendering.class;
			} else {
				return clazz;
			}
		}
	}
	
	private class RenderingTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			PropertyInterface property = properties.get(column);
			value = Rendering.render(value, RenderType.PLAIN_TEXT, property);

			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}

	@Override
	public Dimension getMinimumSize() {
		Dimension header = table.getTableHeader().getMinimumSize();
		return new Dimension(0, header.height + 10 * table.getRowHeight());
	};

	@Override
	public Dimension getPreferredSize() {
		Dimension minimum = getMinimumSize();
		return new Dimension(30000, minimum.height);
	}
	
	@Override
	public Dimension getMaximumSize() {
		return new Dimension(30000, 30000);
	};

}
