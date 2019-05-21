package org.minimalj.frontend.impl.swing.toolkit;

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

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;

public class SwingSearchPanel<T> extends JPanel implements IContent {
	private static final long serialVersionUID = 1L;
	private final JTextField text;
	private final JButton searchButton;
	private final SwingTable<T> table;
	
	public SwingSearchPanel(final Search<T> search, Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		super(new BorderLayout());
		
		text = new JTextField();
		searchButton = new JButton("Search");
		table = new SwingTable<>(keys, multiSelect, listener);

		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.add(text, BorderLayout.CENTER);
		northPanel.add(searchButton, BorderLayout.EAST);
				
		add(border(northPanel, 5, 5, 5, 5), BorderLayout.NORTH);
		add(table, BorderLayout.CENTER);

		text.addActionListener(e -> searchButton.doClick());
		
		searchButton.addActionListener(e -> {
			List<T> objects = search.search(text.getText());
			table.setObjects(objects);
		});
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
