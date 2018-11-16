package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.Rendering;
import org.minimalj.util.CloneHelper;

public class SwingComboBox<T> extends JComboBox<T> implements Input<T> {
	private static final long serialVersionUID = 1L;
	
	private final InputComponentListener listener;
	private final NullableComboBoxModel<T> model;
	
	public SwingComboBox(List<T> objects, InputComponentListener listener) {
		this.listener = listener;
		setRenderer(new CodeItemRenderer(getRenderer()));
		addItemListener(new ComboBoxChangeListener());
		setInheritsPopupMenu(true);
		model = new NullableComboBoxModel<T>(objects);
		setModel(model);
	}
	
	@Override
	public void setValue(T object) {
		model.setObject(object);
	}

	@Override
	public T getValue() {
		return model.getSelectedObject();
	}

	@Override
	public void setEditable(boolean enabled) {
		super.setEnabled(enabled);
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
			listener.changed(SwingComboBox.this);
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			SwingFrontend.runWithContext(this::fireChangeEvent);
		}
	}
	
	private static class NullableComboBoxModel<T> extends AbstractListModel<T> implements ComboBoxModel<T> {
		private static final long serialVersionUID = 1L;
		private final List<T> objects;
		private T setObject;
		private T selectedObject;
		private boolean setObjectInObjects;
		
		public NullableComboBoxModel(List<T> objects) {
			this.objects = Objects.requireNonNull(objects);
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
		public T getElementAt(int index) {
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
		
		private void updateSetObjectInObjects() {
			setObjectInObjects = setObject == null || objects.contains(setObject);
		}
	}
	
	private class CodeItemRenderer implements ListCellRenderer<T> {

		private final ListCellRenderer<? super T> delegate;
		
		public CodeItemRenderer(ListCellRenderer<? super T> listCellRenderer) {
			this.delegate = listCellRenderer;
		}
		
		@Override
		public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean cellHasFocus) {
			Component component = delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof Rendering) {
				Rendering renderingValue = (Rendering) value;
				if (component instanceof JLabel) {
					String text = Rendering.toString(value);
					((JLabel) component).setText(text);
					String tooltip = Rendering.toDescriptionString(value);
					if (tooltip != null) {
						((JComponent) component).setToolTipText(tooltip);
					}
				} else {
					if (Configuration.isDevModeActive()) {
						throw new RuntimeException("Cell component expected to be a JLabel");
					} else {
						Logger.getLogger(this.getClass().getName()).warning("Cell component expected to be a JLabel");
					}
				}
			}
			return component;
		}
		
	}
	
}

