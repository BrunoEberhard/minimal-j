package org.minimalj.frontend.swing.toolkit;

import java.awt.Component;

import javax.swing.JScrollPane;

import org.minimalj.frontend.toolkit.IComponent;

public class SwingScrollPane extends JScrollPane implements IComponent {
	private static final long serialVersionUID = 1L;

	public SwingScrollPane(Component view) {
		super(view);
		setBorder(null);
	}

}
