package org.minimalj.frontend.swing;

import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.text.DefaultEditorKit;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.Separator;
import org.minimalj.frontend.swing.lookAndFeel.LookAndFeelAction;
import org.minimalj.frontend.swing.lookAndFeel.PrintLookAndFeel;
import org.minimalj.frontend.swing.lookAndFeel.TerminalLargeFontLookAndFeel;
import org.minimalj.frontend.swing.lookAndFeel.TerminalLookAndFeel;
import org.minimalj.frontend.swing.toolkit.SwingFrontend;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public class SwingMenuBar extends JMenuBar {
	private static final long serialVersionUID = 1L;
	
	private final SwingTab tab;

	public SwingMenuBar(SwingTab tab) {
		super();
		this.tab = tab;

		add(createWindowMenu());
		add(createEditMenu());
		add(createViewMenu());
	}
	
	private JMenu createWindowMenu() {
		JMenu menu = menu("window");
		
		menu.add(new JMenuItem(tab.frame.newWindowAction));
		menu.add(new JMenuItem(tab.frame.closeWindowAction));
		menu.addSeparator();		
		menu.add(new JMenuItem(tab.frame.newTabAction));
		menu.add(new JMenuItem(tab.closeTabAction));
		menu.addSeparator();
		menu.add(new JMenuItem(tab.frame.loginAction));
		menu.add(new JMenuItem(tab.frame.logoutAction));
		menu.addSeparator();
		menu.add(new JMenuItem(tab.frame.exitAction));
		return menu;
	}
	
	private JMenu createEditMenu() {
		JMenu menu = menu("edit");
		menu.add(new JMenuItem(SwingResourceAction.initProperties(new DefaultEditorKit.CutAction(), "cut")));
		menu.add(new JMenuItem(SwingResourceAction.initProperties(new DefaultEditorKit.CopyAction(), "copy")));
		menu.add(new JMenuItem(SwingResourceAction.initProperties(new DefaultEditorKit.PasteAction(), "paste")));
		return menu;
	}
	
	private JMenu createViewMenu() {
		JMenu menu = menu("view");
		menu.add(new JMenuItem(tab.previousAction));
		menu.add(new JMenuItem(tab.nextAction));
		menu.add(new JMenuItem(tab.refreshAction));
		menu.addSeparator();
		menu.add(new JCheckBoxMenuItem(tab.toggleMenuAction));
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
