package ch.openech.mj.swing.toolkit;

import java.awt.Component;

import javax.swing.JScrollPane;

import ch.openech.mj.toolkit.IComponent;

public class SwingScrollPane extends JScrollPane implements IComponent {

	public SwingScrollPane(Component view) {
		super(view);
		setBorder(null);
	}

}
