package org.minimalj.frontend.impl.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.impl.swing.component.WrapLayout;
import org.minimalj.frontend.page.Page;

public class SwingDashboardContent extends JPanel implements IContent {
	private static final long serialVersionUID = 1L;

	public SwingDashboardContent(List<Page> dashes) {
		super(new WrapLayout(FlowLayout.LEFT));
		
		for (Page dash : dashes) {
			JPanel container = new JPanel(new BorderLayout());
			container.setPreferredSize(new Dimension(550, 350));
			container.add(new JLabel(dash.getTitle()), BorderLayout.NORTH);
			container.add((Component) dash.getContent(), BorderLayout.CENTER);
			add(container);
		}
	}
	
}
