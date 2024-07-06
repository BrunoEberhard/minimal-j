package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
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
import org.minimalj.frontend.form.element.ComboBoxFormElement;
import org.minimalj.model.Rendering;
import org.minimalj.util.CloneHelper;

public class SwingComboBox<T> extends JComboBox<T> implements Input<T> {
	private static final long serialVersionUID = 1L;
	
	private final InputComponentListener listener;
	private final NullableComboBoxModel<T> model;
	private final String nullText;
	
	public SwingComboBox(List<T> objects, String nullText, InputComponentListener listener) {
		this.listener = listener;
		this.nullText = nullText;
		setRenderer(new CodeItemRenderer(getRenderer()));
		addItemListener(new ComboBoxChangeListener());
		setInheritsPopupMenu(true);
		model = new NullableComboBoxModel<>(objects, nullText != ComboBoxFormElement.NO_NULL_STRING);
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
			SwingFrontend.run(SwingComboBox.this, this::fireChangeEvent);
		}
	}

	private static class NullableComboBoxModel<T> extends AbstractListModel<T> implements ComboBoxModel<T> {
		private static final long serialVersionUID = 1L;
		private final List<T> objects;
		private T setObject;
		private boolean setObjectInObjects;
		private final boolean hasNull;
		private final int nullCount;
		
		public NullableComboBoxModel(List<T> objects, boolean hasNull) {
			this.objects = new ArrayList<>(Objects.requireNonNull(objects));
			this.hasNull = hasNull;
			this.nullCount = hasNull ? 1 : 0;
		}

		@Override
		public int getSize() {
			if (setObjectInObjects) {
				return objects.size() + nullCount;
			} else {
				return objects.size() + 1 + nullCount;
			}
		}

		@Override
		public T getElementAt(int index) {
			if (index == 0 && hasNull) {
				return null;
			}
			index = index - nullCount;
			if (setObjectInObjects) {
				return objects.get(index);
			} else {
				if (index == 0) {
					return setObject;
				} else {
					return objects.get(index - 1);
				}
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void setSelectedItem(Object anObject) {
			setObject = (T) anObject;
			if (!setObjectInObjects) {
				setObjectInObjects = true;
				fireContentsChanged(this, -1, -1);
			}
		}

		@Override
		public Object getSelectedItem() {
			return setObject;
		}

		protected T getSelectedObject() {
			return setObject;
		}
		
		protected void setObject(T object) {
			try {
				this.setObject = CloneHelper.clone(object);
			} catch (Exception x) {
				// pretty ugly but needed
				// CodeItem cannot be cloned, but changeable (domain) objects have to because they could be changed after the set
				this.setObject = object;
			}
			this.setObject = object;
			boolean setObjectInObjects = this.setObjectInObjects;
			updateSetObjectInObjects();
			if (setObjectInObjects != this.setObjectInObjects) {
				fireContentsChanged(this, -1, -1);
			}
		}
		
		private void updateSetObjectInObjects() {
			setObjectInObjects = hasNull && setObject == null || objects.contains(setObject);
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
			if (component instanceof JLabel) {
				String text = value != null || nullText == ComboBoxFormElement.NO_NULL_STRING ? Rendering.toString(value) : nullText;
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
			return component;
		}
		
	}
	
}

