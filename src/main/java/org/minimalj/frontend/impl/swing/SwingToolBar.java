package org.minimalj.frontend.impl.swing;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.frontend.page.Routing;

import com.formdev.flatlaf.extras.FlatSVGIcon;

public class SwingToolBar extends JToolBar {
	private static final long serialVersionUID = 1L;
	
	private final JTextField textFieldSearch = new JTextField();
	private final JButton buttonBack = new JButton();
	private final JButton buttonForward = new JButton();
	private final JButton buttonRefresh = new JButton();
	private final JButton buttonPrevious = new JButton();
	private final JButton buttonNext = new JButton();
	private final JToggleButton buttonFilter = new JToggleButton();
	private final JButton buttonFavorite = new JButton();
	private final JTextField indexLabel = new JTextField();
	
	public SwingToolBar() {
		setFloatable(false);
		buttonFilter.setSelectedIcon(new FlatSVGIcon(Swing.class.getPackage().getName().replace(".", "/") + "/filterActive.svg"));
		indexLabel.setMinimumSize(new Dimension(80, 10));
		indexLabel.setMaximumSize(new Dimension(200, indexLabel.getMaximumSize().height));
		indexLabel.setEditable(false);
		indexLabel.setBorder(null);
		indexLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		fillToolBar();
	}
	
	public void setActiveTab(SwingTab tab) {
		buttonBack.setAction(tab.backAction);
		buttonForward.setAction(tab.forwardAction);
		buttonRefresh.setAction(tab.refreshAction);
		buttonFavorite.setAction(tab.favoriteAction);
		buttonPrevious.setAction(tab.previousAction);
		buttonNext.setAction(tab.nextAction);
		buttonFilter.setAction(tab.filterAction);
		indexLabel.setDocument(tab.indexDocument);
	}

	protected void fillToolBar() {
		fillToolBarNavigation();
		fillToolBarRefresh();
		if (Routing.available()) {
			fillToolBarFavorite();
		}
		fillToolBarTable();
		add(Box.createHorizontalGlue());
		fillToolBarSearch();
	}
	
	protected void fillToolBarNavigation() {
		add(buttonBack);
		add(buttonForward);
	}
	
	protected void fillToolBarRefresh() {
		add(buttonRefresh);
	}
	
	protected void fillToolBarFavorite() {
		add(buttonFavorite);
	}
	
	protected void fillToolBarTable() {
		add(buttonFilter);
		add(buttonPrevious);
		add(buttonNext);
		add(indexLabel);
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
			Application.getInstance().search(query);
		}));
		textFieldSearch.setEnabled(Application.getInstance().hasSearch());
	}

	public void setSearchEnabled(boolean hasSearchPages) {
		textFieldSearch.setEnabled(hasSearchPages);
	}
	
}
