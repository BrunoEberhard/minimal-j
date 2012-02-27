package ch.openech.mj.vaadin.toolkit;

import ch.openech.mj.toolkit.GridFormLayout;
import ch.openech.mj.toolkit.IComponent;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;

public class VaadinGridFormLayout extends GridLayout implements GridFormLayout {

	private final int columns;
	private final int defaultSpan;
	private int column, row;
	
	public VaadinGridFormLayout(int columns, int defaultSpan) {
		super(columns, 1);
		this.columns = columns;
		this.defaultSpan = defaultSpan;
		setSpacing(true);
		setMargin(true);
		addStyleName("gridForm");
		
		setWidth(300 * columns + "px");
	}

	@Override
	public void add(String caption, IComponent field) {
		add(caption, field, defaultSpan);
	}

	@Override
	public void add(String caption, IComponent field, int span) {
		Component component = VaadinClientToolkit.getComponent(field);
		component.setCaption(caption);
		component.setWidth("100%");

		setRows(row+1); // addComponent with these arguments doenst auto grow grid
		addComponent(component, column, row, column + span -1, row);
		
		column += span;
		if (column >= columns) {
			column = 0;
			row++;
		}
	}

	@Override
	public void addArea(String caption, IComponent field, int span) {
		add(caption, field, span);
	}

}
