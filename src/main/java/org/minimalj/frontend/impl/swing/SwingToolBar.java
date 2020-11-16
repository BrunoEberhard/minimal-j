package org.minimalj.frontend.impl.swing;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.Routing;

public class SwingToolBar extends JToolBar {
	private static final long serialVersionUID = 1L;
	
	private final JTextField textFieldSearch = new JTextField();
	private final JButton buttonPrevious = new JButton();
	private final JButton buttonNext = new JButton();
	private final JButton buttonRefresh = new JButton();
	private final JButton buttonFavorite = new JButton();
	private SwingTab activeTab;

	public SwingToolBar() {
		setFloatable(false);
		fillToolBar();
	}
	
	public void setActiveTab(SwingTab tab) {
		this.activeTab = tab;

		buttonPrevious.setAction(tab.previousAction);
		buttonNext.setAction(tab.nextAction);
		buttonRefresh.setAction(tab.refreshAction);
		buttonFavorite.setAction(tab.favoriteAction);
	}

	protected void fillToolBar() {
		fillToolBarNavigation();
		fillToolBarRefresh();
		if (Routing.available()) {
			fillToolBarFavorite();
		}
		add(Box.createHorizontalGlue());
		fillToolBarSearch();
	}
	
	protected void fillToolBarNavigation() {
		add(buttonPrevious);
		add(buttonNext);
	}
	
	protected void fillToolBarRefresh() {
		add(buttonRefresh);
	}
	
	protected void fillToolBarFavorite() {
		add(buttonFavorite);
	}
	
	protected void fillToolBarSearch() {
		Dimension size = new Dimension(200, textFieldSearch.getPreferredSize().height);
		textFieldSearch.setMinimumSize(size);
		textFieldSearch.setPreferredSize(size);
		textFieldSearch.setMaximumSize(size);
		add(textFieldSearch);
		Dimension rightFillerDimension = new Dimension(6, 0);
		add(new Box.Filler(rightFillerDimension, rightFillerDimension, rightFillerDimension));
		textFieldSearch.addActionListener(e -> SwingFrontend.run(e, () -> {
			String query = textFieldSearch.getText();
			Page page = Application.getInstance().createSearchPage(query);
			activeTab.show(page);
		}));
		textFieldSearch.setEnabled(Application.getInstance().hasSearchPages());
	}

	public void setSearchEnabled(boolean hasSearchPages) {
		textFieldSearch.setEnabled(hasSearchPages);
	}
	
}
