package org.minimalj.frontend.impl.lanterna.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.form.element.FormElementConstraint;
import org.minimalj.frontend.impl.lanterna.toolkit.LanternaCaption;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.LayoutManager;
import com.googlecode.lanterna.gui2.Panel;


public class LanternaForm extends Panel implements FormContent, LayoutManager {

	private final List<List<Component>> rows = new ArrayList<>();
	private final Map<Component, Integer> spans = new HashMap<Component, Integer>();
	private final Map<IComponent, LanternaCaption> captionByComponent = new HashMap<>();
	private final int columns;

	private List<Component> actualRow = new ArrayList<>();
	private int actualColumn;
	
    public LanternaForm(int columns) {
    	if (columns < 1) throw new IllegalArgumentException(LanternaForm.class.getSimpleName() + " can only work with at least 1 column");
    	setLayoutManager(this);
    	this.columns = columns;
    	createNewRow();
    }
    
	private void createNewRow() {
		actualRow = new ArrayList<>();
		rows.add(actualRow);
		actualColumn = 0;
	}

	@Override
	public void add(IComponent component, FormElementConstraint constraint) {
		createNewRow();
		Component lanternaComponent = (Component) component;
		actualRow.add(lanternaComponent);
		actualColumn = columns;
		spans.put((Component) component, columns);
		super.addComponent((Component) component);
	}

	@Override
	public void add(String caption, IComponent component, FormElementConstraint constraint, int span) {
		LanternaCaption lanternaComponent = new LanternaCaption((Component) component, caption);
		captionByComponent.put(component, lanternaComponent);
		if (actualColumn >= columns) {
			createNewRow();
		}
		actualRow.add(lanternaComponent);
		actualColumn += span;
		spans.put(lanternaComponent, span);
		super.addComponent(lanternaComponent);
	}

	@Override
	public void setValidationMessages(IComponent component, List<String> validationMessages) {
		LanternaCaption caption = captionByComponent.get(component);
		if (caption != null) {
			caption.setValidationMessages(validationMessages);
		}
	}
	
	@Override
	public void doLayout(TerminalSize area, List<Component> components) {
		int terminalRow = 0;
		int columnSize = area.getColumns() / columns; // TODO Round correct
		for (List<Component> row : rows) {
			int terminalColumn = 0;
			int height = 1;
			for (Component c : row) {
				height = Math.max(height, c.getPreferredSize().getRows());
			}
			
			for (Component c : row) {
				TerminalPosition position = new TerminalPosition(terminalColumn, terminalRow);
				TerminalSize size = new TerminalSize(spans.get(c) * columnSize, height);
				c.setPosition(position);
				c.setSize(size);
				terminalColumn += size.getColumns();
			}
			terminalRow += height + 1;
		}
	}

	@Override
	public TerminalSize getPreferredSize(List<Component> components) {
		int sumHeight = -1;
		for (List<Component> row : rows) {
			int height = 1;
			for (Component c : row) {
				height = Math.max(height, c.getPreferredSize().getRows());
			}
			sumHeight += height + 1;
		}
		
		return new TerminalSize(columns * 40, Math.max(0, sumHeight));
	}

	@Override
	public boolean hasChanged() {
		return false;
	}
}
