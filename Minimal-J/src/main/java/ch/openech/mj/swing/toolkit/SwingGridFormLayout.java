package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.LabelUI;
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
	public void add(String caption, IComponent field) {
		add(caption, field, 1);
	}

	@Override
	public void add(String caption, IComponent field, int span) {
		add(caption(caption, field), "spanx " + span + ", growx, aligny top");
	}

	@Override
	public void addArea(String caption, IComponent field, int span) {
		Component component = caption(caption, field);
		add(component, "spanx " + span + ", push, growx, aligny top");
	}

	private Component caption(String caption, IComponent field) {
		Component component = SwingClientToolkit.getComponent(field);
		if (caption != null) {
			JPanel panel = new JPanel(new BorderLayout());
			panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
			panel.add(createCaptionLabel(caption), BorderLayout.NORTH);
			panel.add(component, BorderLayout.CENTER);
			
			return panel;
		} else {
			return component;
		}
	}

	public JLabel createCaptionLabel(String caption) {
		JLabel label = new CaptionLabel(caption);
		return label;
	}
	
	private static class CaptionLabel extends JLabel {

		public CaptionLabel(String caption) {
			super(caption);
		}

		@Override
		public void setUI(LabelUI ui) {
			super.setUI(ui);
			Font font = getFont();
			float fontSize = font.getSize();
			fontSize = (float)((int)(fontSize * 4.0F / 5.0F + 1.0F));
			setFont(font.deriveFont(fontSize).deriveFont(Font.BOLD));
		}
		
	}

}
