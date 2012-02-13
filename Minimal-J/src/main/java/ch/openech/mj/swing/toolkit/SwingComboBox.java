package ch.openech.mj.swing.toolkit;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.edit.fields.Focusable;
import ch.openech.mj.swing.component.IndicatingComboBox;
import ch.openech.mj.toolkit.ComboBox;

public class SwingComboBox extends IndicatingComboBox implements ComboBox, Focusable {

	private ComboBoxChangeListener changeListener;

	public SwingComboBox() {
	}
	
	@Override
	public void setChangeListener(ChangeListener listener) {
		if (changeListener == null) {
			changeListener = new ComboBoxChangeListener();
		}
		changeListener.setChangeListener(listener);
	}

	@Override
	public void setObjects(List<?> objects) {
		setModel(new DefaultComboBoxModel(objects.toArray()));
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
		private ChangeListener changeListener;
		
		public void setChangeListener(ChangeListener changeListener) {
			if (changeListener == null) {
				if (this.changeListener != null) {
					removeItemListener(this);
				}
			} else {
				if (this.changeListener == null) {
					addItemListener(this);
				}
			}		
			this.changeListener = changeListener;
		}

		private void fireChangeEvent() {
			changeListener.stateChanged(new ChangeEvent(SwingComboBox.this));
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				fireChangeEvent();
			}
		}
	}
	
}

