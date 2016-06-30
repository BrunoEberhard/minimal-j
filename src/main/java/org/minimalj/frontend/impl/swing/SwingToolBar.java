package org.minimalj.frontend.impl.swing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.frontend.page.Page;

public class SwingToolBar extends JToolBar {
	private static final long serialVersionUID = 1L;
	
	private final SwingTab tab;
	private JTextField textFieldSearch;

	public SwingToolBar(SwingTab tab) {
		super();
		this.tab = tab;
		
		setFloatable(false);
		fillToolBar();
	}
	
	protected void fillToolBar() {
		fillToolBarNavigation();
		fillToolBarRefresh();
		add(Box.createHorizontalGlue());
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
		textFieldSearch = new JTextField();
		textFieldSearch.setPreferredSize(new Dimension(200, textFieldSearch.getPreferredSize().height));
		textFieldSearch.setMaximumSize(textFieldSearch.getPreferredSize());
		add(textFieldSearch);
		Dimension rightFillerDimension = new Dimension(6, 0);
		add(new Box.Filler(rightFillerDimension, rightFillerDimension, rightFillerDimension));
		if (Application.getInstance().hasSearchPages()) {
			textFieldSearch.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					SwingFrontend.runWithContext(() -> {
						String query = textFieldSearch.getText();
						Page page = Application.getInstance().createSearchPage(query);
						tab.show(page);
					});
				}
			});
		} else {
			textFieldSearch.setEnabled(false);
		}
	}
	
	void onHistoryChanged() {
		// nothing to do right now
	}
	
}
