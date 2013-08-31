package ch.openech.mj.swing.toolkit;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ch.openech.mj.toolkit.FlowField;
import ch.openech.mj.toolkit.IComponent;


public class SwingFlowField extends JPanel implements FlowField {
	private JLabel lastLabel;
	
	public SwingFlowField() {
		super(new MigLayout("ins 4 1 0 0, gap 0 0, wrap 1"));
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	@Override
	public void clear() {
		removeAll();
		repaint();
		revalidate();
	}

	@Override
	public void addGap() {
		if (lastLabel != null) {
			lastLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		}
	}

	@Override
	public void add(IComponent component) {
		super.add((Component) component);
		repaint();
		revalidate();
	}
	
}
