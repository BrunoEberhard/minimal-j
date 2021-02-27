package org.minimalj.frontend.impl.swing;

import java.awt.event.ActionEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.Separator;
import org.minimalj.frontend.impl.swing.lookAndFeel.LookAndFeelAction;
import org.minimalj.frontend.impl.swing.lookAndFeel.PrintLookAndFeel;
import org.minimalj.frontend.impl.swing.lookAndFeel.TerminalLargeFontLookAndFeel;
import org.minimalj.frontend.impl.swing.lookAndFeel.TerminalLookAndFeel;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.frontend.page.Routing;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public class SwingMenuBar extends JMenuBar {
	private static final long serialVersionUID = 1L;
	
	private final SwingFrame frame;
	private JMenu menuFavorite;
	private final JMenuItem itemPrevious = new JMenuItem();
	private final JMenuItem itemNext = new JMenuItem();
	private final JMenuItem itemRefresh = new JMenuItem();
	private final JMenuItem itemFavorite = new JMenuItem();
	private SwingTab activeTab;

	public SwingMenuBar(SwingFrame frame) {
		super();
		this.frame = frame;

		add(createWindowMenu());
		add(createViewMenu());
		if (Routing.available()) {
			add(createFavoriteMenu());
		}
	}
	
	public void setActiveTab(SwingTab tab) {
		this.activeTab = tab;

		itemPrevious.setAction(tab.previousAction);
		itemNext.setAction(tab.nextAction);
		itemRefresh.setAction(tab.refreshAction);
		itemFavorite.setAction(tab.favoriteAction);
	}

	private JMenu createWindowMenu() {
		JMenu menu = menu("window");
		
		menu.add(new JMenuItem(frame.newWindowAction));
		menu.add(new JMenuItem(frame.closeWindowAction));
		menu.addSeparator();		
		menu.add(new JMenuItem(frame.newTabAction));
		menu.add(new JMenuItem(frame.closeTabAction));
		if (frame.loginAction != null) {
			menu.addSeparator();
			menu.add(new JMenuItem(frame.loginAction));
			if (frame.logoutAction != null)  {
				menu.add(new JMenuItem(frame.logoutAction));
			}
		}
		menu.addSeparator();
		menu.add(new JMenuItem(frame.exitAction));
		return menu;
	}
	
	private JMenu createViewMenu() {
		JMenu menu = menu("view");
		menu.add(itemPrevious);
		menu.add(itemNext);
		menu.add(itemRefresh);
		menu.addSeparator();
		menu.add(new JCheckBoxMenuItem(frame.navigationAction));
		menu.addSeparator();
		menu.add(createLookAndFeeldMenu());
		return menu;
	}

	private JMenu createLookAndFeeldMenu() {
		JMenu menu = menu("lookAndFeel");
		menu.add(new JMenuItem(new LookAndFeelAction("system")));
		menu.add(new JMenuItem(new LookAndFeelAction("highContrast", TerminalLookAndFeel.class.getName())));
		menu.add(new JMenuItem(new LookAndFeelAction("highContrastLarge", TerminalLargeFontLookAndFeel.class.getName())));
		menu.add(new JMenuItem(new LookAndFeelAction("print", PrintLookAndFeel.class.getName())));
		return menu;
	}

	private JMenu createFavoriteMenu() {
		menuFavorite = menu("favorites");
		LinkedHashMap<String, String> favorites = frame.favorites.getFavorites();
		updateFavorites(favorites);
		return menuFavorite;
	}

	void updateFavorites(LinkedHashMap<String, String> favorites) {
		if (menuFavorite != null) {
			menuFavorite.removeAll();
			for (Entry<String, String> favorite : favorites.entrySet()) {
				menuFavorite.add(new JMenuItem(new ShowFavoriteAction(favorite.getKey(), favorite.getValue())));
			}
			if (favorites.isEmpty()) {
				JMenuItem item = new JMenuItem(Resources.getString("Menu.favorites.empty"));
				item.setEnabled(false);
				menuFavorite.add(item);
			}
		}
	}
	
	private class ShowFavoriteAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		private final String route;
		
		public ShowFavoriteAction(String route, String title) {
			super(title);
			this.route = route;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			activeTab.show(Routing.createPageSafe(route));
		}
	}
	
	//
	
	private JMenu menu(String resourceName) {
		String text = Resources.getString("Menu." + resourceName);
		JMenu menu = new JMenu(text);
		Integer mnemonic = SwingResourceAction.getKeyCode("Menu." + resourceName + ".mnemonic");
		if (mnemonic != null) {
			menu.setMnemonic(mnemonic);
		} else if (!StringUtils.isEmpty(text)) {
			menu.setMnemonic(text.charAt(0));
		}
		return menu;
	}
	
	public static void addActions(JMenu menu, List<Action> actions) {
		for (Action action : actions) {
			if (action instanceof org.minimalj.frontend.action.ActionGroup) {
				org.minimalj.frontend.action.ActionGroup actionGroup = (org.minimalj.frontend.action.ActionGroup) action;
				JMenu subMenu = new JMenu(SwingFrontend.adaptAction(action));
				addActions(subMenu, actionGroup.getItems());
				menu.add(subMenu);
			} else if (action instanceof Separator) {
				menu.addSeparator();
			} else {
				menu.add(new JMenuItem(SwingFrontend.adaptAction(action)));
			}
		}
	}
}	
