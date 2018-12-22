package org.minimalj.frontend.impl.lanterna.toolkit;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.LinearLayout.Alignment;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;

public class LanternaSearchTable<T> extends Panel implements IContent {
	private final TextBox textBox;
	private final Button searchButton;
	private final LanternaTable<T> table;

	public LanternaSearchTable(Search<T> search, Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		super(new LinearLayout(Direction.VERTICAL));

		setPreferredSize(new TerminalSize(30, 10));

		Panel northPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
		addComponent(northPanel, LinearLayout.createLayoutData(Alignment.Beginning));

		table = new LanternaTable<>(keys, multiSelect, listener);
		table.setVisibleRows(5);
		addComponent(table, LinearLayout.createLayoutData(Alignment.Center));

		this.textBox = new TextBox();
		northPanel.addComponent(textBox, LinearLayout.createLayoutData(Alignment.Center));

		Runnable runnable = () -> table.setObjects(search.search(textBox.getText()));
		this.searchButton = new Button("..", () -> LanternaFrontend.run(textBox, runnable));
		searchButton.setRenderer(new Button.FlatButtonRenderer());
		northPanel.addComponent(searchButton, LinearLayout.createLayoutData(Alignment.End));
	}


}
