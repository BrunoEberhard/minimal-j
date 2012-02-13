package ch.openech.mj.swing.component;

import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.JPanel;

public class JPanelBaseline extends JPanel {

	public JPanelBaseline() {
	}

	public JPanelBaseline(LayoutManager layout) {
		super(layout);
	}

	public JPanelBaseline(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public JPanelBaseline(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	@Override
	public int getBaseline(int width, int height) {
		int baseline = super.getBaseline(width, height);
		if (baseline < 0) {
			baseline = baseline(width, height, true);
		}
		if (baseline < 0) {
			baseline = baseline(width, height, false);
		}
		return baseline;
	}
	
	private int baseline(int width, int height, boolean mustBeVisible) {
		for (Component component : getComponents()) {
			if (component.isVisible()) {
				int baseline = component.getBaseline(width, height);
				if (baseline >= 0) return baseline;
			}
		}
		return -1;
	}
	
}
