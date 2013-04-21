package ch.openech.mj.swing.lookAndFeel;

public class TerminalLargeFontLookAndFeel extends TerminalLookAndFeel {

	@Override
	public MetalTheme getMetalTheme() {
		return new TerminalLookAndFeel.MetalTheme(true);
	}

}
