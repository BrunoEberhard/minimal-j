package ch.openech.mj.vaadin.toolkit;

import ch.openech.mj.toolkit.GridFormLayout;

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
	}

	@Override
	public void add(String caption, Object field) {
		add(caption, field, defaultSpan);
	}

	@Override
	public void add(String caption, Object field, int span) {
		Component component = (Component) field;
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
	public void addArea(String caption, Object field, int span) {
		Component component = (Component) field;
		component.setHeight(100, Sizeable.UNITS_PIXELS);
		
		add(caption, field, span);
	}

}
