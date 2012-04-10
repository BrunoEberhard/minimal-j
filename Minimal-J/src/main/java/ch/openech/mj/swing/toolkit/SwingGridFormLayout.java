package ch.openech.mj.swing.toolkit;

import java.awt.Component;
import java.awt.FontMetrics;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.PanelUI;

import net.miginfocom.swing.MigLayout;
import ch.openech.mj.toolkit.GridFormLayout;
import ch.openech.mj.toolkit.IComponent;

public class SwingGridFormLayout extends JPanel implements GridFormLayout {

	public SwingGridFormLayout(int columns, int columnWidthPercentage) {
		String columnConstraints = "";
		int columnWidth = getColumnWidth() * 100 / columnWidthPercentage;
		for (int i = 0; i<columns; i++) {
			columnConstraints += "[" + columnWidth + "px:" + columnWidth + "px:" + (columnWidth * 2) + "px, grow 1, push, sizegroup c]";
		}
		setLayout(new MigLayout("ins 5, gapy 0px, wrap " + columns, columnConstraints));
		
		setBorder(null);
	}
	
	private int getColumnWidth() {
		FontMetrics fm = getFontMetrics(getFont());
		return (int)fm.getStringBounds("The quick brown fox jumps over the lazy dog", getGraphics()).getWidth() / 2;
	}
	
	@Override
	public void setUI(PanelUI ui) {
		// Hauptsächlich für Windows nötig
		UIManager.put("TextField.inactiveBackground", UIManager.get("TextField.background"));
		super.setUI(ui);
	}
	
	@Override
	public void add(IComponent field) {
		add(field, 1);
	}

	@Override
	public void add(IComponent field, int span) {
		add(SwingClientToolkit.getComponent(field), "spanx " + span + ", growx, aligny top");
	}

	@Override
	public void addArea(IComponent field, int span) {
		Component component = SwingClientToolkit.getComponent(field);
		add(component, "spanx " + span + ", growx, aligny top");
	}

}
