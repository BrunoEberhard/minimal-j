package org.minimalj.frontend.swing.toolkit;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;
import org.minimalj.frontend.toolkit.ClientToolkit.Search;
import org.minimalj.frontend.toolkit.TextField;

public class SwingTextFieldAutocomplete extends JComboBox<String> implements TextField {
	private static final long serialVersionUID = -1;

	public SwingTextFieldAutocomplete(InputComponentListener changeListener, Search<String> searchable) {
		setEditable(true);
		Component c = getEditor().getEditorComponent();
		if (c instanceof JTextComponent) {
			JTextComponent tc = (JTextComponent) c;
			tc.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void changedUpdate(DocumentEvent arg0) {
					changeListener.changed(SwingTextFieldAutocomplete.this);
				}

				@Override
				public void insertUpdate(DocumentEvent arg0) {
					update();
					changeListener.changed(SwingTextFieldAutocomplete.this);
				}

				@Override
				public void removeUpdate(DocumentEvent arg0) {
					update();
					changeListener.changed(SwingTextFieldAutocomplete.this);
				}

				private void update() {
					// perform separately, as listener conflicts between the editing component
					// and JComboBox will result in an IllegalStateException due to editing
					// the component when it is locked.
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							List<String> founds = new ArrayList<String>(searchable.search(tc.getText()));
							Set<String> foundSet = new HashSet<String>();
							for (String s : founds) {
								foundSet.add(s.toLowerCase());
							}
							Collections.sort(founds);// sort alphabetically
							setEditable(false);
							removeAllItems();

							// if founds contains the search text, then only add once.
							if (!foundSet.contains(tc.getText().toLowerCase())) {
								addItem(tc.getText());
							}

							for (String s : founds) {
								addItem(s);
							}

							setEditable(true);
							setPopupVisible(true);
						}
					});
				}
			});

			// When the text component changes, focus is gained
			// and the menu disappears. To account for this, whenever the focus
			// is gained by the JTextComponent and it has searchable values, we
			// show the popup.

			tc.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent arg0) {
					if (tc.getText().length() > 0) {
						setPopupVisible(true);
					}
				}
			});
		} else {
			throw new IllegalStateException("Editing component is not a JTextComponent!");
		}
	}

	@Override
	public void setValue(String text) {
		super.setSelectedItem(text);
	}

	@Override
	public String getValue() {
		return (String) super.getSelectedItem();
	}

	@Override
	public void setCommitListener(Runnable runnable) {
		// TODO Auto-generated method stub
	}

}