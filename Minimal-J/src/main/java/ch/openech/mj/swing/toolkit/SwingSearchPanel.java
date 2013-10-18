package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ch.openech.mj.search.Search;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.ITable.TableActionListener;

public class SwingSearchPanel<T> extends JPanel implements IComponent {
	private static final long serialVersionUID = 1L;
	private final JTextField text;
	private final JButton searchButton;
	private final SwingTable<T> table;
	
	public SwingSearchPanel(final Search<T> search, TableActionListener<T> listener) {
		super(new BorderLayout());
		
		text = new JTextField();
		searchButton = new JButton("Search");
		table = new SwingTable<T>(search.getClazz(), search.getKeys());

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
				table.setObjects(search.search(text.getText()));
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
