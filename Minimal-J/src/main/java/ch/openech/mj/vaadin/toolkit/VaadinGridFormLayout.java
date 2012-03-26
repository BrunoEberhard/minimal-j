package ch.openech.mj.vaadin.toolkit;

import ch.openech.mj.toolkit.GridFormLayout;
import ch.openech.mj.toolkit.IComponent;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;

public class VaadinGridFormLayout extends GridLayout implements GridFormLayout {

	private final int columns;
	private int column, row;
	
	public VaadinGridFormLayout(int columns, int columnWidthPercentage) {
		super(columns, 1);
		this.columns = columns;
		setSpacing(true);
		setMargin(true);
		addStyleName("gridForm");
		
		// ex is the height of the letter x
		// its just a best guess to make it a little bit depending on Font size
		setWidth(columnWidthPercentage * columns / 3 + "ex");
	}

	@Override
	public void add(String caption, IComponent field) {
		add(caption, field, 1);
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
//		Component component = VaadinClientToolkit.getComponent(field);
//		component.setHeight("100%");
		
//		setRowExpandRatio(row, 1.0F);
		add(caption, field, span);
	}

}
