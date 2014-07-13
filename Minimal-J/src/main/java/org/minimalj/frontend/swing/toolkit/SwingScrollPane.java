package org.minimalj.frontend.swing.toolkit;

import java.awt.Component;

import javax.swing.JScrollPane;

import org.minimalj.frontend.toolkit.ClientToolkit.IContent;

public class SwingScrollPane extends JScrollPane implements IContent {
	private static final long serialVersionUID = 1L;

	public SwingScrollPane(Component view) {
		super(view);
		setBorder(null);
	}

}
