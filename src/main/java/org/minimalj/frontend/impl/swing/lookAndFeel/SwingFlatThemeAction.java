package org.minimalj.frontend.impl.swing.lookAndFeel;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.minimalj.frontend.impl.swing.SwingResourceAction;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.util.SystemInfo;

public class SwingFlatThemeAction extends SwingResourceAction {
	private static final long serialVersionUID = 1L;
	private final boolean light;
	
	public SwingFlatThemeAction(boolean light) {
		super("LookAndFeel." + (light ? "flatLight" : "flatDark"));
		this.light = light;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		if (SystemInfo.isMacOS) {
			if (light) FlatMacLightLaf.setup(); else FlatMacDarkLaf.setup();
		} else {
			if (light) FlatLightLaf.setup(); else FlatDarkLaf.setup();
		}
		SwingFrontend.setUIManagerProperties();
		FlatLaf.revalidateAndRepaintAllFramesAndDialogs();
		for (Window window : JFrame.getWindows()) {
			SwingUtilities.updateComponentTreeUI(window);
			window.validate();
			window.repaint();
		}
	}

}