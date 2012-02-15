package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.PanelUI;

import ch.openech.mj.toolkit.GridFormLayout;
import ch.openech.mj.toolkit.IComponent;

public class SwingGridFormLayout extends JPanel implements GridFormLayout {

	private final int columns, defaultSpan;
	private int gridx, gridy;
	private boolean canVerticalGrow = false;
	
	public SwingGridFormLayout(int columns, int defaultSpan) {
		super(new GridBagLayout());
		this.columns = columns;
		this.defaultSpan = defaultSpan;
		
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}
	
	@Override
	public Dimension getPreferredSize() {
		if (canVerticalGrow) {
			return new Dimension(200 * columns, 100000);
		} else {
			return new Dimension(200 * columns, super.getPreferredSize().height);
		}
	}

	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(200 * columns, super.getMinimumSize().height);
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
		GridBagConstraints constraints = createLayoutConstraints(span);
		add(caption(caption, field), constraints);
	}

	@Override
	public void addArea(String caption, IComponent field, int span) {
		GridBagConstraints constraints = createLayoutConstraints(span);
		constraints.weighty = 1.0;
		Component component = caption(caption, field);
		canVerticalGrow = true;
		add(component, constraints);
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

	private GridBagConstraints createLayoutConstraints(int span) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = gridx;
		constraints.gridwidth = span;
		constraints.gridy = gridy;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		
		gridx += span;
		if (gridx >= columns) {
			gridx = 0;
			gridy += 1;
		}
		return constraints;
	}
}
