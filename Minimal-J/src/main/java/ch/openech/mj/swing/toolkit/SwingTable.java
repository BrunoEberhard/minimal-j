package ch.openech.mj.swing.toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import ch.openech.mj.swing.component.PropertyTable;
import ch.openech.mj.toolkit.ITable;

public class SwingTable<T> extends JScrollPane implements ITable<T> {

	private static final long serialVersionUID = 1L;
	
	private final PropertyTable<T> propertyTable;
	private TableActionListener<T> listener;
	
	public SwingTable(Class<T> clazz, Object[] fields) {
		propertyTable = new PropertyTable<T>(clazz, fields);

		propertyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		propertyTable.setRowSelectionAllowed(true);
		propertyTable.setFillsViewportHeight(true);
		
		setViewportView(propertyTable);
		
		bindRowHeightToFont();

		propertyTable.addMouseListener(new SwingTableMouseListener());
	}

	private void bindRowHeightToFont() {
		propertyTable.addPropertyChangeListener("UI", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				propertyTable.setRowHeight(propertyTable.getFont().getSize() * 5 / 3 + 2);
			}
		});
	}

	@Override
	public void setObjects(List<T> objects) {
		propertyTable.setObjects(objects);
	}

	public T getSelectedObject() {
		if (propertyTable.getSelectionModel().getLeadSelectionIndex() >= 0) {
			return propertyTable.getObject(propertyTable.getSelectionModel().getLeadSelectionIndex());
		} else {
			return null;
		}
	}

	public List<T> getSelectedObjects() {
		List<T> selectedObjects = new ArrayList<>(propertyTable.getSelectedRowCount());
		for (int row : propertyTable.getSelectedRows()) {
			selectedObjects.add(propertyTable.getObject(row));
		}
		return selectedObjects;
	}

	@Override
	public void setClickListener(TableActionListener<T> listener) {
		this.listener = listener;
	}

	private class SwingTableMouseListener extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() >= 2 && listener != null) {
				try {
					listener.action(getSelectedObject(), getSelectedObjects());
				} catch (Exception x) {
					x.printStackTrace();
				}
			}
		}
	}

	@Override
	public void setInsertListener(final InsertListener listener) {
		if (listener != null) {
			Action action = new AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					listener.action();
				}
			};
			bindKey(KeyEvent.VK_INSERT, action);
		} else {
			unbindKey(KeyEvent.VK_INSERT);
		}
	}

	@Override
	public void setDeleteListener(final TableActionListener<T> listener) {
		if (listener != null) {
			Action action = new AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					listener.action(getSelectedObject(), getSelectedObjects());
				}
			};
			bindKey(KeyEvent.VK_DELETE, action);
		} else {
			unbindKey(KeyEvent.VK_DELETE);
		}
	}

	@Override
	public void setFunctionListener(final int function, final TableActionListener<T> listener) {
		if (listener != null) {
			Action action = new AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					listener.action(getSelectedObject(), getSelectedObjects());
				}
			};
			bindKey(KeyEvent.VK_F1+function, action);
		} else {
			unbindKey(KeyEvent.VK_F1+function);
		}
	}
	
	private void bindKey(int keyEvent, Action action) {
		getActionMap().put(keyEvent, action);
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(keyEvent, 0), keyEvent);
	}

	private void unbindKey(int keyEvent) {
		getActionMap().remove(keyEvent);
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(KeyStroke.getKeyStroke(keyEvent, 0));
	}

}
