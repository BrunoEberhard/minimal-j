package org.minimalj.frontend.swing.toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;

public class SwingCheckBox extends JCheckBox implements Input<Boolean> {
	private static final long serialVersionUID = 1L;
	
	private final InputComponentListener listener;

	public SwingCheckBox(InputComponentListener listener, String text) {
		super(text, false);
		this.listener = listener;
		addActionListener(new CheckBoxChangeListener());
	}
	
	@Override
	public void setEditable(boolean editable) {
		setEnabled(editable);
	}
	
	public class CheckBoxChangeListener implements ActionListener {
		
		private void fireChangeEvent() {
			listener.changed(SwingCheckBox.this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			fireChangeEvent();
		}
	}

	@Override
	public void setValue(Boolean value) {
		setSelected(Boolean.TRUE.equals(value));
	}

	@Override
	public Boolean getValue() {
		return isSelected();
	}

}

