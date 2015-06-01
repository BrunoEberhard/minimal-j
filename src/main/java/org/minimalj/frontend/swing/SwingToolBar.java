package org.minimalj.frontend.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.minimalj.application.Application;
import org.minimalj.frontend.page.SearchPage;

public class SwingToolBar extends JToolBar {
	private static final long serialVersionUID = 1L;
	
	private final SwingTab tab;
	private JComboBox<SearchPage> comboBoxSearchObject;
	private JTextField textFieldSearch;
	private SearchAction searchAction;

	public SwingToolBar(SwingTab tab) {
		super();
		this.tab = tab;
		
		searchAction = new SearchAction();		
		setFloatable(false);
		fillToolBar();
	}
	
	protected void fillToolBar() {
		fillToolBarNavigation();
		fillToolBarRefresh();
		fillToolBarSearch();
	}
	
	protected void fillToolBarNavigation() {
		add(tab.previousAction);
		add(tab.nextAction);
	}
	
	protected void fillToolBarRefresh() {
		add(tab.refreshAction);
	}
	
	protected void fillToolBarSearch() {
		SearchPage[] searchPages = Application.getApplication().getSearchPages();
		if (searchPages != null && searchPages.length > 0) {
			add(createSearchField());
		}
	}

	protected JPanel createSearchField() {
		FlowLayout flowLayout = new FlowLayout(FlowLayout.TRAILING);
		flowLayout.setAlignOnBaseline(true);
		JPanel panel = new JPanel(flowLayout);
		SearchPage[] searchPages = Application.getApplication().getSearchPages();
		comboBoxSearchObject = new JComboBox<SearchPage>(searchPages);
		comboBoxSearchObject.setRenderer(new SearchCellRenderer());
		panel.add(comboBoxSearchObject);
		textFieldSearch = new JTextField();
		textFieldSearch.setPreferredSize(new Dimension(200, textFieldSearch.getPreferredSize().height));
		panel.add(textFieldSearch);
		final JButton button = new JButton(searchAction);
		button.setHideActionText(true);
		panel.add(button);
		textFieldSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button.doClick();
			}
		});
		return panel;
	}
	
	private static class SearchCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			SearchPage searchPage = (SearchPage) value;
			return super.getListCellRendererComponent(list, searchPage.getName(), index, isSelected, cellHasFocus);
		}
	}
	
	protected class SearchAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			SearchPage searchPage = (SearchPage) comboBoxSearchObject.getSelectedItem();
			String query = textFieldSearch.getText();
			searchPage.setQuery(query);
			tab.show(searchPage);
		}
	}
	
	void onHistoryChanged() {
		// nothing to do right now
	}
	
}
