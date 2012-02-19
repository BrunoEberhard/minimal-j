package ch.openech.mj.swing.toolkit;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import ch.openech.mj.edit.fields.Focusable;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.swing.component.IndicatingList;
import ch.openech.mj.toolkit.VisualList;

public class SwingVisualList extends JScrollPane implements VisualList, Focusable {

	private final IndicatingList list;
	private ClickListener clickListener;
	private SwingVisualListMouseListener mouseListener;
	private SwingVisualListKeyListener keyListener;

	public SwingVisualList() {
		list = new IndicatingList();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setViewportView(list);
		
		setInheritsPopupMenu(true);
		list.setInheritsPopupMenu(true);
	}
	
	@Override
	public void setObjects(List<?> objects) {
		DefaultListModel model = (DefaultListModel) list.getModel();
		model.removeAllElements();
		for (Object object : objects) {
			model.addElement(object);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		setInheritsPopupMenu(enabled);
		setViewportView(enabled ? list : null);
	}

	@Override
	public void setSelectedObject(Object object) {
		for (int i = 0; i<list.getModel().getSize(); i++) {
			if (list.getModel().getElementAt(i).equals(object)) {
				list.getSelectionModel().setSelectionInterval(i, i);
			}
		}
		list.getSelectionModel().clearSelection();
	}

	@Override
	public Object getSelectedObject() {
		int index = list.getSelectionModel().getMinSelectionIndex();
		if (index >= 0) {
			return list.getModel().getElementAt(index);
		} else {
			return null;
		}
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		list.setValidationMessages(validationMessages);
	}

	@Override
	public int getSelectedIndex() {
		return list.getSelectedIndex();
	}
	
	@Override
	public void setClickListener(ClickListener clickListener) {
		if (clickListener == null) {
			if (mouseListener != null) {
				removeMouseListener(mouseListener);
				mouseListener = null;
			}
			if (keyListener != null) {
				removeKeyListener(keyListener);
				keyListener = null;
			}
		}
		this.clickListener = clickListener;
		if (clickListener != null) {
			if (mouseListener == null) {
				mouseListener = new SwingVisualListMouseListener();
				addMouseListener(mouseListener);
			}
			if (keyListener != null) {
				keyListener = new SwingVisualListKeyListener();
				addKeyListener(keyListener);
			}
		}
	}

	private class SwingVisualListMouseListener extends MouseAdapter {
		
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
	
	private class SwingVisualListKeyListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyChar() == '\n' && getSelectedIndex() >= 0) {
				try {
					clickListener.clicked();
				} catch (Exception x) {
					x.printStackTrace();
				}
			}
		}
	}
	
}

