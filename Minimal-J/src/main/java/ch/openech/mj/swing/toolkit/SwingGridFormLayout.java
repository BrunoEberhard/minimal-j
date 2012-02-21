package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.PanelUI;

import net.miginfocom.swing.MigLayout;
import ch.openech.mj.toolkit.GridFormLayout;
import ch.openech.mj.toolkit.IComponent;

public class SwingGridFormLayout extends JPanel implements GridFormLayout {

	private final int defaultSpan;
	
	public SwingGridFormLayout(int columns, int defaultSpan) {
		String columnConstraints = "";
		for (int i = 0; i<columns; i++) {
			columnConstraints += "[200lp!]";
		}
		setLayout(new MigLayout("wrap " + columns, columnConstraints));
		
		this.defaultSpan = defaultSpan;
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}
	
	@Override
	public void setUI(PanelUI ui) {
		// Hauptsächlich für Windows nötig
		UIManager.put("TextField.inactiveBackground", UIManager.get("TextField.background"));
		super.setUI(ui);
	}
	
	@Override
	public void add(String caption, IComponent field) {
		add(caption, field, defaultSpan);
	}

	@Override
	public void add(String caption, IComponent field, int span) {
		add(caption(caption, field), "spanx " + span + ", grow");
	}

	@Override
	public void addArea(String caption, IComponent field, int span) {
		Component component = caption(caption, field);
		add(component, "spanx " + span + ", height 70lp:150lp:150lp, growprioy 100, grow");
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
		JLabel label = new JLabel(caption);
		return label;
	}

}
