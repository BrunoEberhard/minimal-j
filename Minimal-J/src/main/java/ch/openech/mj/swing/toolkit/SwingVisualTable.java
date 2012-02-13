package ch.openech.mj.swing.toolkit;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import ch.openech.mj.toolkit.VisualTable;
import ch.openech.mj.util.PropertyTable;

public class SwingVisualTable<T> extends JScrollPane implements VisualTable<T> {

	private final PropertyTable<T> propertyTable;
	private List<T> objects;
	private ClickListener clickListener;
	private SwingVisualTableMouseListener mouseListener;
	private SwingVisualTableKeyListener keyListener;
	
	public SwingVisualTable(Class<T> clazz, Object[] fields) {
		propertyTable = new PropertyTable<T>(clazz, fields);

		propertyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		propertyTable.setRowSelectionAllowed(true);
		propertyTable.setFillsViewportHeight(true);
		
		setViewportView(propertyTable);
	}

	@Override
	public void setObjects(List<T> objects) {
		propertyTable.setObjects(objects);
		this.objects = objects;
	}

	@Override
	public void setSelectedObject(T object) {
		int index = objects.indexOf(object);
		if (index >= 0) {
			propertyTable.getSelectionModel().setLeadSelectionIndex(index);
		} else {
			propertyTable.getSelectionModel().clearSelection();
		}
	}
	
	@Override
	public T getSelectedObject() {
		if (propertyTable.getSelectionModel().getLeadSelectionIndex() >= 0) {
			return propertyTable.getObject(propertyTable.getSelectionModel().getLeadSelectionIndex());
		} else {
			return null;
		}
	}

	@Override
	public int getSelectedIndex() {
		return propertyTable.getSelectionModel().getLeadSelectionIndex();
	}

	@Override
	public void setClickListener(ClickListener clickListener) {
		if (clickListener == null) {
			if (mouseListener != null) {
				propertyTable.removeMouseListener(mouseListener);
				mouseListener = null;
			}
			if (keyListener != null) {
				propertyTable.removeKeyListener(keyListener);
				keyListener = null;
			}
		}
		this.clickListener = clickListener;
		if (clickListener != null) {
			if (mouseListener == null) {
				mouseListener = new SwingVisualTableMouseListener();
				propertyTable.addMouseListener(mouseListener);
			}
			if (keyListener != null) {
				keyListener = new SwingVisualTableKeyListener();
				propertyTable.addKeyListener(keyListener);
			}
		}
	}
	
	private class SwingVisualTableMouseListener extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			try {
				if (e.getClickCount() >= 2) {
					clickListener.clicked();
				}
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
		
	}
	
	private class SwingVisualTableKeyListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyChar() == '\n' && propertyTable.getSelectedRow() >= 0) {
				try {
					clickListener.clicked();
				} catch (Exception x) {
					x.printStackTrace();
				}
			}
		}
	}

}
