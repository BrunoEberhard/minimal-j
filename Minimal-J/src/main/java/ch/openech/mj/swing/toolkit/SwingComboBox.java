package ch.openech.mj.swing.toolkit;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.db.model.CodeItem;
import ch.openech.mj.edit.value.CloneHelper;
import ch.openech.mj.toolkit.ComboBox;
import ch.openech.mj.toolkit.Focusable;

public class SwingComboBox<T> extends JComboBox implements ComboBox<T>, Focusable {

	private final ChangeListener listener;
	private final NullableComboBoxModel<T> model;
	
	public SwingComboBox(ChangeListener listener) {
		this.listener = listener;
		setRenderer(new CodeItemRenderer(getRenderer()));
		addItemListener(new ComboBoxChangeListener());
		setInheritsPopupMenu(true);
		model = new NullableComboBoxModel<T>();
		setModel(model);
	}
	
	@Override
	public void setObjects(List<T> objects) {
		model.setObjects(objects);
	}

	@Override
	public void setSelectedObject(T object) {
		model.setObject(object);
	}

	@Override
	public T getSelectedObject() {
		return model.getSelectedObject();
	}
	
	//

	/*
	 * There is a problem with cursor selection if <code>null</code> is a element
	 * 
	 * @see http://www.coderanch.com/t/509095/GUI/java/Arrow-navigation-not-working-JComboBox
	 */
	@Override
	public int getSelectedIndex() {
		if (getSelectedItem() == null && getModel().getElementAt(0) == null) {
			return 0;
		} else {
			return super.getSelectedIndex();
		}
	}

	public class ComboBoxChangeListener implements ItemListener {
		
		private void fireChangeEvent() {
			listener.stateChanged(new ChangeEvent(SwingComboBox.this));
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			fireChangeEvent();
		}
	}
	
	private static class NullableComboBoxModel<T> extends AbstractListModel implements ComboBoxModel {
		private List<T> objects = Collections.emptyList();
		private T setObject;
		private T selectedObject;
		private boolean setObjectInObjects;
		
		private NullableComboBoxModel() {
		}

		@Override
		public int getSize() {
			if (setObjectInObjects) {
				return objects.size() + 1;
			} else {
				return objects.size() + 2;			
			}
		}

		@Override
		public Object getElementAt(int index) {
			if (setObjectInObjects) {
				if (index == 0) {
					return null;
				} else {
					return objects.get(index-1);
				}
			} else {
				if (index == 0) {
					return null;
				} else if (index == 1) {
					return setObject;
				} else {
					return objects.get(index-2);
				}
			}
		}

		@Override
		public void setSelectedItem(Object anObject) {
			if (selectedObject != null && !selectedObject.equals(anObject) || selectedObject == null
					&& anObject != null) {
				selectedObject = (T) anObject;
				fireContentsChanged(this, -1, -1);
			}
		}

		@Override
		public Object getSelectedItem() {
			return selectedObject;
		}

		protected T getSelectedObject() {
			return selectedObject;
		}
		
		protected void setObject(T object) {
			try {
				this.setObject = CloneHelper.clone(object);
			} catch (Exception x) {
				// pretty ugly but needed
				// CodeItem cannot be cloned, but changeable (domain) objects have to because they could be changed after the set
				this.setObject = object;
			}
			this.selectedObject = object;
			updateSetObjectInObjects();
			fireContentsChanged(this, -1, -1);
		}
		
		protected void setObjects(List<T> objects) {
			this.objects = objects;
			updateSetObjectInObjects();
			fireContentsChanged(this, -1, -1);
		}
		
		private void updateSetObjectInObjects() {
			setObjectInObjects = (setObject == null || objects.contains(setObject));
		}
	}
	
	
	
	private static class CodeItemRenderer implements ListCellRenderer {

		private final ListCellRenderer delegate;
		
		public CodeItemRenderer(ListCellRenderer delegate) {
			this.delegate = delegate;
		}
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component component = delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (component instanceof JComponent && value instanceof CodeItem) {
				((JComponent) component).setToolTipText(((CodeItem)value).getDescription());
			}
			return component;
		}
		
	}
	
}

