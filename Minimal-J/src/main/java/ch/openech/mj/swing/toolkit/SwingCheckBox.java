package ch.openech.mj.swing.toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.edit.fields.Focusable;
import ch.openech.mj.toolkit.CheckBox;

public class SwingCheckBox extends JCheckBox implements CheckBox, Focusable {

	private final ChangeListener listener;

	public SwingCheckBox(ChangeListener listener, String text) {
		super(text, false);
		this.listener = listener;
		addActionListener(new CheckBoxChangeListener());
	}
	
	public class CheckBoxChangeListener implements ActionListener {
		
		private void fireChangeEvent() {
			listener.stateChanged(new ChangeEvent(SwingCheckBox.this));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			fireChangeEvent();
		}
	}

}

