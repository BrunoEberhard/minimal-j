package org.minimalj.frontend.impl.swing.toolkit;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;

public class SwingTextAreaField extends JScrollPane implements Input<String> {
	private static final long serialVersionUID = 1L;
	
	private final InputComponentListener changeListener;
	private final JTextArea textArea;
	
	public SwingTextAreaField(InputComponentListener changeListener, int maxLength, String pattern) {
		textArea = new JTextArea(new SwingTextField.FilteredDocument(maxLength, pattern));
		textArea.setFont(UIManager.getDefaults().getFont("TextField.font"));
		setBorder(UIManager.getDefaults().getBorder("TextField.border"));
		textArea.setLineWrap(true);
		textArea.setRows(calcRows(maxLength));
		textArea.getDocument().addDocumentListener(new TextFieldChangeListener());
		setViewportView(textArea);
		
		this.changeListener = changeListener;
		
		setOpaque(false);
		textArea.setOpaque(false);
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
	
	// TODO make number of rows in SwingTextAreaField somehow configurable
	private int calcRows(int maxLength) {
		int rows = 1;
		while (maxLength > 1) {
			rows++;
			maxLength = maxLength / 3;
		}
		return rows;
	}

	public class TextFieldChangeListener implements DocumentListener {

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

