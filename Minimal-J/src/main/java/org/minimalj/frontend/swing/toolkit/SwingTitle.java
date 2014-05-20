package org.minimalj.frontend.swing.toolkit;

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.minimalj.frontend.toolkit.IComponent;

public class SwingTitle extends JLabel implements IComponent {
	private static final long serialVersionUID = 1L;

	public SwingTitle(String string) {
		super(string);
		setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		setFont(getFont().deriveFont(Font.BOLD));
	}

}
