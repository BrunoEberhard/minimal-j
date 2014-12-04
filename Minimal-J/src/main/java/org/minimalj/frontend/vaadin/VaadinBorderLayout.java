package org.minimalj.frontend.vaadin;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * BorderLayout mimics {@link java.awt.BorderLayout} in Vaadin.
 * copied from https://vaadin.com/directory#addon/borderlayout
 * 
 */
@SuppressWarnings("serial")
public class VaadinBorderLayout extends VerticalLayout {

	public enum Constraint {
		NORTH, WEST, CENTER, EAST, SOUTH;
	}

	public static final String DEFAULT_MINIMUM_HEIGHT = "50px";

	private VerticalLayout mainLayout;
	private HorizontalLayout centerLayout;

	private String minimumNorthHeight = DEFAULT_MINIMUM_HEIGHT;
	private String minimumSouthHeight = DEFAULT_MINIMUM_HEIGHT;
	private String minimumWestWidth = DEFAULT_MINIMUM_HEIGHT;
	private String minimumEastWidth = DEFAULT_MINIMUM_HEIGHT;

	protected Component north = new Label("");
	protected Component west = new Label("");
	protected Component center = new Label("");
	protected Component east = new Label("");
	protected Component south = new Label("");

	/**
	 * Create a layout structure that mimics the traditional
	 * {@link java.awt.BorderLayout}.
	 */
	public VaadinBorderLayout() {
		mainLayout = new VerticalLayout();
		centerLayout = new HorizontalLayout();
		centerLayout.addComponent(west);
		centerLayout.addComponent(center);
		centerLayout.addComponent(east);
		centerLayout.setSizeFull();

		mainLayout.addComponent(north);
		mainLayout.addComponent(centerLayout);
		mainLayout.addComponent(south);
		mainLayout.setExpandRatio(centerLayout, 1);

		super.setWidth("100%");
		super.addComponent(mainLayout);
	}

	@Override
	public void setWidth(String width) {
		if (mainLayout == null) {
			return;
		}
		mainLayout.setWidth(width);
		centerLayout.setExpandRatio(center, 1);
		requestRepaint();
	}

	@Override
	public void setHeight(String height) {
		mainLayout.setHeight(height);
		west.setHeight("100%");
		center.setHeight("100%");
		east.setHeight("100%");
		centerLayout.setExpandRatio(center, 1);
		requestRepaint();
	}

	@Override
	public void setSizeFull() {
		super.setSizeFull();
		mainLayout.setSizeFull();
		centerLayout.setExpandRatio(center, 1);
		requestRepaint();
	}

	@Override
	public void setMargin(boolean margin) {
		mainLayout.setMargin(margin);
		requestRepaint();
	}

	@Override
	public void setSpacing(boolean spacing) {
		mainLayout.setSpacing(spacing);
		centerLayout.setSpacing(spacing);
		requestRepaint();
	}

	@Override
	public boolean isSpacing() {
		return (mainLayout.isSpacing() && centerLayout.isSpacing());
	}

	@Override
	public void removeComponent(Component c) {
		replaceComponent(c, new Label(""));
	}

	/**
	 * Add component into borderlayout
	 * 
	 * @param c
	 *            component to be added into layout
	 * @param constraint
	 *            place of the component (have to be on of BorderLayout.NORTH,
	 *            BorderLayout.WEST, BorderLayout.CENTER, BorderLayout.EAST, or
	 *            BorderLayout.SOUTH
	 */
	public void addComponent(Component c, Constraint constraint) {
		if (constraint == Constraint.NORTH) {
			mainLayout.replaceComponent(north, c);
			north = c;
			if (north.getHeight() < 0
					|| north.getHeightUnits() == Sizeable.UNITS_PERCENTAGE) {
				north.setHeight(minimumNorthHeight);
			}
		} else if (constraint == Constraint.WEST) {
			centerLayout.replaceComponent(west, c);
			west = c;
			if (west.getWidth() < 0 || west.getWidthUnits() == Sizeable.UNITS_PERCENTAGE) {
				west.setWidth(minimumWestWidth);
			}
		} else if (constraint == Constraint.CENTER) {
			centerLayout.replaceComponent(center, c);
			center = c;
			center.setHeight(centerLayout.getHeight(),
					centerLayout.getHeightUnits());
			center.setWidth("100%");
			centerLayout.setExpandRatio(center, 1);
		} else if (constraint == Constraint.EAST) {
			centerLayout.replaceComponent(east, c);
			east = c;
			if (east.getWidth() < 0 || east.getWidthUnits() == Sizeable.UNITS_PERCENTAGE) {
				east.setWidth(minimumEastWidth);
			}
		} else if (constraint == Constraint.SOUTH) {
			mainLayout.replaceComponent(south, c);
			south = c;
			if (south.getHeight() < 0
					|| south.getHeightUnits() == Sizeable.UNITS_PERCENTAGE) {
				south.setHeight(minimumSouthHeight);
			}
		} else {
			throw new IllegalArgumentException(
					"Invalid BorderLayout constraint.");
		}
		centerLayout.setExpandRatio(center, 1);
		requestRepaint();
	}

	@Override
	public void addComponent(Component c) {
		throw new IllegalArgumentException(
				"Component constraint have to be specified");
	}

	@Override
	public void replaceComponent(Component oldComponent, Component newComponent) {
		if (oldComponent == north) {
			mainLayout.replaceComponent(north, newComponent);
			north = newComponent;
		} else if (oldComponent == west) {
			centerLayout.replaceComponent(west, newComponent);
			west = newComponent;
		} else if (oldComponent == center) {
			centerLayout.replaceComponent(center, newComponent);
			centerLayout.setExpandRatio(newComponent, 1);
			center = newComponent;
		} else if (oldComponent == east) {
			centerLayout.replaceComponent(east, newComponent);
			east = newComponent;
		} else if (oldComponent == south) {
			mainLayout.replaceComponent(south, newComponent);
			south = newComponent;
		}
		centerLayout.setExpandRatio(center, 1);
		requestRepaint();
	}

	/**
	 * Set minimum height of the component in the BorderLayout.NORTH
	 * 
	 * @param minimumNorthHeight
	 */
	public void setMinimumNorthHeight(String minimumNorthHeight) {
		this.minimumNorthHeight = minimumNorthHeight;
	}

	/**
	 * Get minimum height of the component in the BorderLayout.NORTH
	 */
	public String getMinimumNorthHeight() {
		return minimumNorthHeight;
	}

	/**
	 * Set minimum height of the component in the BorderLayout.SOUTH
	 * 
	 * @param minimumSouthHeight
	 */
	public void setMinimumSouthHeight(String minimumSouthHeight) {
		this.minimumSouthHeight = minimumSouthHeight;
	}

	/**
	 * Get minimum height of the component in the BorderLayout.SOUTH
	 */
	public String getMinimumSouthHeight() {
		return minimumSouthHeight;
	}

	/**
	 * Set minimum height of the component in the BorderLayout.WEST
	 * 
	 * @param minimumWestWidth
	 */
	public void setMinimumWestWidth(String minimumWestWidth) {
		this.minimumWestWidth = minimumWestWidth;
	}

	/**
	 * Get minimum height of the component in the BorderLayout.WEST
	 */
	public String getMinimumWestWidth() {
		return minimumWestWidth;
	}

	/**
	 * Set minimum height of the component in the BorderLayout.EAST
	 * 
	 * @param minimumEastWidth
	 */
	public void setMinimumEastWidth(String minimumEastWidth) {
		this.minimumEastWidth = minimumEastWidth;
	}

	/**
	 * Get minimum height of the component in the BorderLayout.EAST
	 */
	public String getMinimumEastWidth() {
		return minimumEastWidth;
	}

}