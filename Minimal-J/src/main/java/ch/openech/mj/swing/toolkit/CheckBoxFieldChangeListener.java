package ch.openech.mj.swing.toolkit;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CheckBoxFieldChangeListener implements ItemListener {

	private JCheckBox checkBox;
	private Object source;
	private ChangeListener changeListener;
	
	public CheckBoxFieldChangeListener(JCheckBox checkBox, Object source) {
		this.source = source;
		this.checkBox = checkBox;
	}

	public void setChangeListener(ChangeListener changeListener) {
		if (changeListener == null) {
			if (this.changeListener != null) {
				checkBox.removeItemListener(this);
			}
		} else {
			if (this.changeListener == null) {
				checkBox.addItemListener(this);
			}
		}		
		this.changeListener = changeListener;
	}
	
	private void fireChangeEvent() {
		changeListener.stateChanged(new ChangeEvent(source));
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getID() == ItemEvent.ITEM_STATE_CHANGED) {
			fireChangeEvent();
		}
	}

}
