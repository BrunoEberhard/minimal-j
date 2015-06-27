package org.minimalj.frontend.swing.toolkit;

import java.awt.Component;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.minimalj.frontend.toolkit.ClientToolkit.Input;
import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;

public class SwingTextFieldAutocomplete extends JComboBox<String> implements Input<String> {
	private static final long serialVersionUID = -1;

	public SwingTextFieldAutocomplete(InputComponentListener changeListener, List<String> suggestions) {
		super(suggestions.toArray(new String[suggestions.size()]));
		setEditable(true);
		Component c = getEditor().getEditorComponent();
		if (c instanceof JTextComponent) {
			JTextComponent tc = (JTextComponent) c;
			tc.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void changedUpdate(DocumentEvent arg0) {
					changeListener.changed(SwingTextFieldAutocomplete.this);
					hideIfNoMatchingItem();
				}

				@Override
				public void insertUpdate(DocumentEvent arg0) {
					changeListener.changed(SwingTextFieldAutocomplete.this);
					hideIfNoMatchingItem();
				}

				@Override
				public void removeUpdate(DocumentEvent arg0) {
					changeListener.changed(SwingTextFieldAutocomplete.this);
					hideIfNoMatchingItem();
				}
			});
		} else {
			throw new IllegalStateException("Editing component is not a JTextComponent!");
		}
	}

	private void hideIfNoMatchingItem() {
		if (isPopupVisible()) {
			String selectedItem = (String) getSelectedItem();
			if (selectedItem != null && !selectedItem.startsWith(getValue())) {
				hidePopup();
			}
		}
	}

	@Override
	public int getSelectedIndex() {
		if (getEditor() == null) {
			return -1;
		}
		
		String value = getValue();
		if (value == null) {
			return -1;
		} else {
			int index = -1;
			for (int i = 0; i < getModel().getSize(); i++) {
				String item = getModel().getElementAt(i);
				if (item != null && item.startsWith(value)) {
					index = i;
					break;
				}
			}
			return index;
		}
	}	
	
	@Override
	public void setValue(String text) {
		super.setSelectedItem(text);
	}

	@Override
	public String getValue() {
		Component c = getEditor().getEditorComponent();
		JTextComponent tc = (JTextComponent) c;
		return tc.getText();
	}

}