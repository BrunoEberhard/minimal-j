package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;

public class SwingTextAreaField extends JScrollPane implements Input<String> {
	private static final long serialVersionUID = 1L;
	
	private final InputComponentListener changeListener;
	private final JTextArea textArea;
	
	public SwingTextAreaField(InputComponentListener changeListener, int maxLength, String pattern) {
		textArea = new JTextArea(new SwingTextField.FilteredDocument(maxLength, pattern)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void processComponentKeyEvent(KeyEvent e) {
				if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_TAB) {
					e.consume();
					if (e.isShiftDown()) {
						transferFocusBackward();
					} else {
						transferFocus();
					}
				} else {
					super.processComponentKeyEvent(e);
				}
			}
		};
		textArea.setLineWrap(true);
		// textArea.setRows(calcRows(maxLength));
		textArea.getDocument().addDocumentListener(new TextFieldChangeListener());
		setViewportView(textArea);
		
		this.changeListener = changeListener;
		
		// not yet supported
//		textArea.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				if (commitListener != null) {
//					commitListener.run();
//				}
//			}
//		});
	}
	
	@Override
	public synchronized void addMouseListener(MouseListener l) {
		super.addMouseListener(l);
		textArea.addMouseListener(l);
	}
	
	// TODO make number of rows in SwingTextAreaField somehow configurable
	private int calcRows(int maxLength) {
		int rows = 1;
		while (maxLength > 1) {
			rows++;
			maxLength = maxLength / 3;
		}
		return rows;
	}

	public class TextFieldChangeListener implements DocumentListener, Runnable {
		private boolean invokeSet = false;

		@Override
		public void changedUpdate(DocumentEvent arg0) {
			fireChangeEvent();
		}

		@Override
		public void insertUpdate(DocumentEvent arg0) {
			fireChangeEvent();
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
			fireChangeEvent();
		}

		private void fireChangeEvent() {
			// gather all remove/insert of document in one change
			if (!invokeSet) {
				invokeSet = true;
				SwingUtilities.invokeLater(this);
			}
		}

		@Override
		public void run() {
			invokeSet = false;
			changeListener.changed(SwingTextAreaField.this);
		}
	}

	@Override
	public void setValue(String value) {
		textArea.setText(value);
	}

	@Override
	public String getValue() {
		return textArea.getText();
	}

	@Override
	public void setEditable(boolean editable) {
		textArea.setEditable(editable);
	}
}

