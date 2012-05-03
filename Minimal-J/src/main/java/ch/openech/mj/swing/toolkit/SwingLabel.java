package ch.openech.mj.swing.toolkit;

import javax.swing.JLabel;

import ch.openech.mj.toolkit.IComponent;

public class SwingLabel extends JLabel implements IComponent {

	public SwingLabel(String string) {
		super("<html>" + string + "</html>");
	}

}
