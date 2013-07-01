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
	public void add(IComponent c, int span) {
		Component component = (Component) c;
		int w = span * columnWidth; // minimum und prefered size
		if (SwingClientToolkit.verticallyGrowing(component)) {
			add(component, "spanx " + span + ", growx, aligny top, width " + w + "px:" + w + "px");
		} else {
			add(component, "spanx " + span + ", growx, aligny top, sizegroupy h, width " + w + "px:" + w + "px");
		}
	}

}
