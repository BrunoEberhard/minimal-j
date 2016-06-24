package org.minimalj.frontend.impl.lanterna;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.lanterna.component.HighContrastLanternaTheme;
import org.minimalj.frontend.impl.lanterna.toolkit.LanternaFrontend;

import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.swing.SwingTerminal;

public class Lanterna {

	private Lanterna() {
		// private
	}

	public void run() {
		try {
			Frontend.setInstance(new LanternaFrontend());

			SwingTerminal terminal = new SwingTerminal();
			Screen screen = new Screen(terminal);

			LanternaGUIScreen gui = new LanternaGUIScreen(screen);
			gui.setTheme(new HighContrastLanternaTheme());

			screen.startScreen();
			gui.init();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	public static void main(final String[] args) throws Exception {
		Application.initApplication(args);

		new Lanterna().run();
	}
}
