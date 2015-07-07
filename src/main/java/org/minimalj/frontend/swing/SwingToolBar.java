package org.minimalj.frontend.swing;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.minimalj.application.Application;
import org.minimalj.frontend.page.Page;

public class SwingToolBar extends JToolBar {
	private static final long serialVersionUID = 1L;
	
	private final SwingTab tab;
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
		add(createSearchField());
	}

	protected JPanel createSearchField() {
		FlowLayout flowLayout = new FlowLayout(FlowLayout.TRAILING);
		flowLayout.setAlignOnBaseline(true);
		JPanel panel = new JPanel(flowLayout);
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
	
	protected class SearchAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			String query = textFieldSearch.getText();
			Page page = Application.getApplication().createSearchPage(query);
			tab.show(page);
		}
	}
	
	void onHistoryChanged() {
		// nothing to do right now
	}
	
}
