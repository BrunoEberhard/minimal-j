package ch.openech.mj.swing.toolkit;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.edit.fields.Focusable;
import ch.openech.mj.toolkit.ComboBox;

public class SwingComboBox extends JComboBox implements ComboBox, Focusable {

	private final ChangeListener listener;
	
	public SwingComboBox(ChangeListener listener) {
		this.listener = listener;
		addItemListener(new ComboBoxChangeListener());
		setInheritsPopupMenu(true);
	}
	
	@Override
	public void setObjects(List<?> objects) {
		setModel(new NullableComboBoxModel(objects));
	}

	@Override
	public void setSelectedObject(Object object) {
		setSelectedItem(object);
	}

	@Override
	public Object getSelectedObject() {
		return getSelectedItem();
	}

	public class ComboBoxChangeListener implements ItemListener {
		
		private void fireChangeEvent() {
			listener.stateChanged(new ChangeEvent(SwingComboBox.this));
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				fireChangeEvent();
			}
		}
	}
	
	private static class NullableComboBoxModel extends AbstractListModel implements ComboBoxModel {
		private List<?> objects;
		private Object selectedObject;
		
		private NullableComboBoxModel(List<?> objects) {
			this.objects = objects;
		}

		@Override
		public int getSize() {
			return objects.size() + 1;
		}

		@Override
		public Object getElementAt(int index) {
			if (index == 0) {
				return "";
			} else {
				return objects.get(index-1);
			}
		}

		@Override
		public void setSelectedItem(Object anObject) {
			if (selectedObject != null && !selectedObject.equals(anObject) || selectedObject == null
					&& anObject != null) {
				selectedObject = anObject;
				fireContentsChanged(this, -1, -1);
			}
		}

		@Override
		public Object getSelectedItem() {
			return selectedObject;
		}
	}
	
}

