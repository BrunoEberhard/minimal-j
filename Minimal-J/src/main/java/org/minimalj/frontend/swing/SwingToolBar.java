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

import org.minimalj.application.MjApplication;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageContext;
import org.minimalj.frontend.page.PageLink;
import org.minimalj.frontend.toolkit.IComponent;
import org.minimalj.util.resources.Resources;

public class SwingToolBar extends JToolBar implements IComponent {
	private static final long serialVersionUID = 1L;
	
	private final SwingTab tab;
	private JComboBox<Class<?>> comboBoxSearchObject;
	private JTextField textFieldSearch;
	private SearchAction searchAction;

	public SwingToolBar(SwingTab tab) {
		super();
		this.tab = tab;
		
		searchAction = new SearchAction();		
		setFloatable(false);
		fillToolBar(tab);
	}
	
	protected void fillToolBar(PageContext pageContext) {
		fillToolBarNavigation();
		fillToolBarRefresh();
		fillToolBarUpDown();
		fillToolBarSearch(pageContext);
	}
	
	protected void fillToolBarNavigation() {
		add(tab.previousAction);
		add(tab.nextAction);
	}
	
	protected void fillToolBarRefresh() {
		add(tab.refreshAction);
	}
	
	protected void fillToolBarUpDown() {
		add(tab.upAction);
		add(tab.downAction);
	}
	
	protected void fillToolBarSearch(PageContext pageContext) {
		Class<?>[] searchClasses = MjApplication.getApplication().getSearchClasses(null);
		if (searchClasses != null && searchClasses.length > 0) {
			add(createSearchField(pageContext));
		}
	}

	protected JPanel createSearchField(PageContext pageContext) {
		FlowLayout flowLayout = new FlowLayout(FlowLayout.TRAILING);
		flowLayout.setAlignOnBaseline(true);
		JPanel panel = new JPanel(flowLayout);
		comboBoxSearchObject = new JComboBox<>(MjApplication.getApplication().getSearchClasses(pageContext));
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
			Class<?> searchClass = (Class<?>) value;
			value = Resources.getString("Search." + searchClass.getSimpleName());
			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
	}
	
	protected class SearchAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			Class<? extends Page> searchObject = (Class<? extends Page>) comboBoxSearchObject.getSelectedItem();
			String text = textFieldSearch.getText();
			search(searchObject, text);
		}
	}
	
	public void search(Class<? extends Page> searchClass, String text) {
		tab.show(PageLink.link(searchClass, text));
	}
	
	void onHistoryChanged() {
		// nothing to do right now
	}
	
}
