package ch.openech.mj.swing.toolkit;

import java.awt.Component;
import java.awt.FontMetrics;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ch.openech.mj.toolkit.GridFormLayout;
import ch.openech.mj.toolkit.IComponent;

public class SwingGridFormLayout extends JPanel implements GridFormLayout {
	private final int columnWidth;
	
	public SwingGridFormLayout(int columns, int columnWidthPercentage) {
		String columnConstraints = "";
		columnWidth = getColumnWidth() * columnWidthPercentage / 100;
		for (int i = 0; i<columns; i++) {
			columnConstraints += "[grow 1, push, sizegroup c]";
		}
		setLayout(new MigLayout("ins 5, gapy 0px, wrap " + columns, columnConstraints));
		
		setBorder(null);
	}
	
	private int getColumnWidth() {
		FontMetrics fm = getFontMetrics(getFont());
		return (int)fm.getStringBounds("The quick brown fox jumps over the lazy dog", getGraphics()).getWidth();
	}
	
	@Override
	public void add(IComponent field) {
		add(field, 1);
	}

	@Override
	public void add(IComponent field, int span) {
		int w = span * columnWidth; // minimum und prefered size
		add((Component) field, "spanx " + span + ", growx, aligny top, sizegroupy h, width " + w + "px:" + w + "px");
	}

	@Override
	public void addArea(IComponent field, int span) {
		Component component = (Component) field;
		int w = span * columnWidth;
		add(component, "spanx " + span + ", growx, aligny top, width " + w + "px:" + w + "px");
	}

}
