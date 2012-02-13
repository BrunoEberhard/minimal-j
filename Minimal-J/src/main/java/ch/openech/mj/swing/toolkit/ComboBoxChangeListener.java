package ch.openech.mj.swing.toolkit;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

// should be obsolet with the new Toolkit-idea
@Deprecated
public class ComboBoxChangeListener implements ItemListener {

	private JComboBox comboBox;
	private Object source;
	private ChangeListener changeListener;
	
	public ComboBoxChangeListener(JComboBox comboBox, Object source) {
		this.source = source;
		this.comboBox = comboBox;
	}

	public void setChangeListener(ChangeListener changeListener) {
		if (changeListener == null) {
			if (this.changeListener != null) {
				comboBox.removeItemListener(this);
			}
		} else {
			if (this.changeListener == null) {
				comboBox.addItemListener(this);
			}
		}		
		this.changeListener = changeListener;
	}

	private void fireChangeEvent() {
		changeListener.stateChanged(new ChangeEvent(source));
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			fireChangeEvent();
		}
	}
}
