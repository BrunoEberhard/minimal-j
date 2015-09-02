package org.minimalj.frontend.impl.swing.lookAndFeel;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

import org.minimalj.frontend.impl.swing.SwingResourceAction;

public class LookAndFeelAction extends SwingResourceAction {
	private static final long serialVersionUID = 1L;

	public LookAndFeelAction(String name) {
		this(name, UIManager.getSystemLookAndFeelClassName());
	}

	public LookAndFeelAction(String name, String laf) {
		super("LookAndFeel." + name);
		putValue(ACTION_COMMAND_KEY, laf);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		try {
			String lookAndFeelClassName = actionEvent.getActionCommand();
			Class<LookAndFeel> lookAndFeelClass = (Class<LookAndFeel>) Class.forName(lookAndFeelClassName);
			LookAndFeel lookAndFeel = lookAndFeelClass.newInstance();
			if (lookAndFeel instanceof MetalThemeProvider) {
				MetalTheme theme = ((MetalThemeProvider) lookAndFeel).getMetalTheme();
				MetalLookAndFeel.setCurrentTheme(theme);
			}
			UIManager.setLookAndFeel(lookAndFeel);
			for (Window window : JFrame.getWindows()) {
				SwingUtilities.updateComponentTreeUI(window);
				window.validate();
				window.repaint();
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(null, "Can't change look and feel", "Invalid PLAF", JOptionPane.ERROR_MESSAGE);
		}
	}

}
