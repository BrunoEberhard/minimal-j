package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.Component;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.util.StringUtils;

public class SwingTextFieldAutocomplete extends JComboBox<String> implements Input<String>, PopupMenuListener {
	private static final long serialVersionUID = -1;

	private final JTextComponent tc;
	private final Search<String> suggestionSearch;
	
	public SwingTextFieldAutocomplete(InputComponentListener changeListener, Search<String> suggestionSearch) {
		super(new SearchListDataModel());
		this.suggestionSearch = suggestionSearch;
		setEditable(true);
		Component c = getEditor().getEditorComponent();
		if (c instanceof JTextComponent) {
			tc = (JTextComponent) c;
			tc.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void changedUpdate(DocumentEvent arg0) {
					changeListener.changed(SwingTextFieldAutocomplete.this);
					if (!adjusting)	hidePopup();
				}

				@Override
				public void insertUpdate(DocumentEvent arg0) {
					changeListener.changed(SwingTextFieldAutocomplete.this);
					if (!adjusting)	hidePopup();
				}

				@Override
				public void removeUpdate(DocumentEvent arg0) {
					changeListener.changed(SwingTextFieldAutocomplete.this);
					if (!adjusting)	hidePopup();
				}
			});
		} else {
			throw new IllegalStateException("Editing component is not a JTextComponent!");
		}
		addPopupMenuListener(this);
	}
	
	private boolean adjusting = false;
	
	@Override
    public void configureEditor(ComboBoxEditor anEditor, Object anItem) {
		adjusting = true;
		super.configureEditor(anEditor, anItem);
		adjusting = false;
    }
    
	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		adjusting = true;
		updateModel(tc);
		adjusting = false;
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
		// nothing to do
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		// nothing to do
	}
	
	private void updateModel(JTextComponent tc) {
		SearchListDataModel model = (SearchListDataModel) getModel();
		model.setQuery(suggestionSearch, tc.getText());
	}

	private static class SearchListDataModel extends AbstractListModel<String> implements ComboBoxModel<String> {
		private static final long serialVersionUID = 1L;
		
		private String lastSearch;
		private List<String> elements = Collections.emptyList();
		private String selection;
		
		public void setQuery(Search<String> suggestions, String query) {
			if (!StringUtils.equals(lastSearch, query)) {
				lastSearch = query;
				elements = suggestions.search(query);
				if (elements.isEmpty()) {
					elements = Collections.singletonList(query);
				} 
				selection = elements.get(0);
				fireContentsChanged(this, 0, elements.size());
			}
		}
		
		@Override
		public int getSize() {
			return elements != null ? elements.size() : 0;
		}
	
		@Override
		public String getElementAt(int index) {
			return elements.get(index);
		}

		@Override
		public void setSelectedItem(Object selection) {
			this.selection = (String) selection;
			fireContentsChanged(this, -1, -1);
		}

		@Override
		public Object getSelectedItem() {
			return selection;
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