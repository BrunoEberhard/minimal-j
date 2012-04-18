package ch.openech.mj.vaadin.toolkit;

import ch.openech.mj.toolkit.GridFormLayout;
import ch.openech.mj.toolkit.IComponent;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

public class VaadinGridFormLayout extends GridLayout implements GridFormLayout {

	private final int columns;
	private final int columnWidthPercentage;
	private int column, row;
	
	public VaadinGridFormLayout(int columns, int columnWidthPercentage) {
		super(columns, 1);
		this.columns = columns;
		this.columnWidthPercentage = columnWidthPercentage;
		
		setSpacing(true);
		setMargin(true);
		addStyleName("gridForm");
	}
	
	public int getDialogWidth() {
		return columnWidthPercentage * columns / 3;
	}

	@Override
	public void add(IComponent field) {
		add(field, 1);
	}

	@Override
	public void add(IComponent field, int span) {
		Component component = VaadinClientToolkit.getComponent(field);
		if (component instanceof Label) {
			component.setWidth(columnWidthPercentage * span / 3 + "ex");
		} else {
			component.setWidth("100%");
		}
		
		setRows(row+1); // addComponent with these arguments doenst auto grow grid
		addComponent(component, column, row, column + span -1, row);
		
		column += span;
		if (column >= columns) {
			column = 0;
			row++;
		}
		
	}

	@Override
	public void addArea(IComponent field, int span) {
		add(field, span);
	}

}
