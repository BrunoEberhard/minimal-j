package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;

public class SwingSearchLayout extends JPanel implements IComponent {
	private static final long serialVersionUID = 1L;

	public SwingSearchLayout(TextField text, Action searchAction, IComponent content, Action[] actions) {
		this(createHeaderComponent(text, searchAction), content, actions);
	}

	private SwingSearchLayout(Component header, IComponent content, Action[] actions) {
		super(new BorderLayout());
		add(border(header, 5, 5, 5, 5), BorderLayout.NORTH);
		add((Component) content, BorderLayout.CENTER);
		add(border(new ButtonBar(actions), 5, 0, 5, 2), BorderLayout.SOUTH);
	}

	private static Component createHeaderComponent(TextField text, Action searchAction) {
		JPanel panel = new JPanel(new BorderLayout());
		JTextField jTextField = (JTextField) text;
		final JButton searchButton = ButtonBar.createButton(searchAction);
		
		panel.add(jTextField, BorderLayout.CENTER);
		panel.add(searchButton, BorderLayout.EAST);

		jTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchButton.doClick();
			}
		});
		return panel;
	}
	
	private static Component border(Component component, int top, int left, int bottom, int right) {
		JComponent jComponent;
		if (component instanceof JComponent) {
			jComponent = (JComponent) component;
		} else {
			jComponent = new JPanel(new BorderLayout());
			jComponent.add(component, BorderLayout.CENTER);
		}
		jComponent.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
		return jComponent;
	}

}
