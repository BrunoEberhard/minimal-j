package org.minimalj.frontend.lanterna.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.minimalj.frontend.lanterna.toolkit.LanternaCaption;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.FormContent;

import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.TextGraphics;
import com.googlecode.lanterna.gui.component.AbstractContainer;
import com.googlecode.lanterna.terminal.TerminalPosition;
import com.googlecode.lanterna.terminal.TerminalSize;

public class LanternaForm extends AbstractContainer implements FormContent {

	private final List<List<Component>> rows = new ArrayList<>();
	private final Map<Component, Integer> spans = new HashMap<Component, Integer>();
	private final Map<IComponent, LanternaCaption> captionByComponent = new HashMap<>();
	private final int columns;

	private List<Component> actualRow = new ArrayList<>();
	private int actualColumn;
	private boolean hasArea;
	
    public LanternaForm(int columns) {
    	if (columns < 1) throw new IllegalArgumentException(LanternaForm.class.getSimpleName() + " can only work with at least 1 column");
    	this.columns = columns;
    	createNewRow();
    }
    
	private void createNewRow() {
		actualRow = new ArrayList<>();
		rows.add(actualRow);
		actualColumn = 0;
	}

	@Override
	public void add(IComponent component) {
		createNewRow();
		Component lanternaComponent = (Component) component;
		actualRow.add(lanternaComponent);
		actualColumn = columns;
		spans.put((Component) component, columns);
		super.addComponent((Component) component);
	}

	@Override
	public void add(String caption, IComponent component, int span) {
		LanternaCaption lanternaComponent = new LanternaCaption((Component) component, caption);
		captionByComponent.put(component, lanternaComponent);
		if (actualColumn >= columns) {
			createNewRow();
		}
		actualRow.add(lanternaComponent);
		actualColumn += span;
		spans.put((Component) component, span);
		super.addComponent((Component) component);
//		hasArea |= verticalGrow; // TODO
	}

	@Override
	public void setValidationMessages(IComponent component, List<String> validationMessages) {
		LanternaCaption caption = captionByComponent.get(component);
		caption.setValidationMessages(validationMessages);
	}

	@Override
	public void repaint(TextGraphics graphics) {
		int terminalRow = 0;
		int columnSize = graphics.getWidth() / columns; // TODO Round correct
		for (List<Component> row : rows) {
			int terminalColumn = 0;
			for (Component c : row) {
				TerminalPosition position = new TerminalPosition(terminalColumn, terminalRow * 3);
				TerminalSize size = new TerminalSize(spans.get(c) * columnSize, 2);
				TextGraphics subSubGraphics = graphics.subAreaGraphics(position, size);
				if(c.isVisible()) {
					try {
						c.repaint(subSubGraphics);
					} catch (Exception x) {
						x.printStackTrace();
					}
				}
				terminalColumn += size.getColumns();
			}
			terminalRow++;
		}
	}

	@Override
	protected TerminalSize calculatePreferredSize() {
		// TODO calculate instead of guess
		TerminalSize size = new TerminalSize(columns * 40, rows.size() * 3);
		return size;
	}

}
