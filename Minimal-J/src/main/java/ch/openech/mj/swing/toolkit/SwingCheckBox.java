package ch.openech.mj.swing.toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.edit.fields.Focusable;
import ch.openech.mj.toolkit.CheckBox;

public class SwingCheckBox extends JCheckBox implements CheckBox, Focusable {

	private CheckBoxChangeListener changeListener;

	public SwingCheckBox(String text) {
		super(text, false);
	}
	
	@Override
	public void setChangeListener(ChangeListener listener) {
		if (changeListener == null) {
			changeListener = new CheckBoxChangeListener();
			addActionListener(changeListener);
		}
		changeListener.setChangeListener(listener);
	}

	public class CheckBoxChangeListener implements ActionListener {
		private ChangeListener changeListener;
		
		public void setChangeListener(ChangeListener changeListener) {
			if (changeListener == null) {
				if (this.changeListener != null) {
					removeActionListener(this);
				}
			} else {
				if (this.changeListener == null) {
					addActionListener(this);
				}
			}		
			this.changeListener = changeListener;
		}

		private void fireChangeEvent() {
			changeListener.stateChanged(new ChangeEvent(SwingCheckBox.this));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			fireChangeEvent();
		}
	}

}

