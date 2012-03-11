package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.util.StringUtils;

public class SwingEditorLayout extends JPanel implements IComponent {

	public SwingEditorLayout(String information, IComponent content, Action[] actions) {
		super(new BorderLayout());
		addInformation(information);
		JScrollPane scrollPane = new JScrollPane(new ScrollablePanel(SwingClientToolkit.getComponent(content)));
		scrollPane.setBorder(null);
		add(scrollPane, BorderLayout.CENTER);
		ButtonBar buttonBar = new ButtonBar(actions);
		buttonBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 2)); // unknown: why the need for additional 2 pixel?
		add(buttonBar, BorderLayout.SOUTH);
	}

	protected void addInformation(String information) {
		if (!StringUtils.isBlank(information)) {
			JLabel help = new JLabel(information);
			help.setBorder(BorderFactory.createEmptyBorder(7, 10, 10, 7));
			add(help, BorderLayout.NORTH);
		}
	}

}
