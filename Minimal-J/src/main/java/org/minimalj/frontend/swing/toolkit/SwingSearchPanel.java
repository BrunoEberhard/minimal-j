package org.minimalj.frontend.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.minimalj.frontend.toolkit.IComponent;
import org.minimalj.frontend.toolkit.ClientToolkit.Search;
import org.minimalj.frontend.toolkit.ITable.TableActionListener;

public class SwingSearchPanel<T> extends JPanel implements IComponent {
	private static final long serialVersionUID = 1L;
	private final JTextField text;
	private final JButton searchButton;
	private final SwingTable<T> table;
	
	public SwingSearchPanel(final Search<T> search, Object[] keys, TableActionListener<T> listener) {
		super(new BorderLayout());
		
		text = new JTextField();
		searchButton = new SwingHeavyActionButton("Search");
		table = new SwingTable<T>(keys);

		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.add(text, BorderLayout.CENTER);
		northPanel.add(searchButton, BorderLayout.EAST);
				
		add(border(northPanel, 5, 5, 5, 5), BorderLayout.NORTH);
		add((Component) table, BorderLayout.CENTER);

		text.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchButton.doClick();
			}
		});
		
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<T> objects = search.search(text.getText());
				table.setObjects(objects);
			}
		});
		
		table.setClickListener(listener);
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
