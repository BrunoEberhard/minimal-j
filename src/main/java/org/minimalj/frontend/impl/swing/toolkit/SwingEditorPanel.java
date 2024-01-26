package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentInputMapUIResource;

import org.minimalj.frontend.Frontend.IContent;

public class SwingEditorPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public SwingEditorPanel(IContent content, List<org.minimalj.frontend.action.Action> actions) {
		this((Component) content, actions);
	}
	
	public SwingEditorPanel(Component content, List<org.minimalj.frontend.action.Action> actions) {
		super(new BorderLayout());
		if (content instanceof JComponent) {
			((JComponent) content).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		}
		
		JScrollPane scrollPane = new JScrollPane(new ScrollablePanel(content));
		scrollPane.setBorder(new TopBottomBorder(scrollPane.getBorder()));
		add(scrollPane, BorderLayout.CENTER);
		
		ButtonBar buttonBar = new ButtonBar(SwingFrontend.adaptActions(actions));
		add(buttonBar, BorderLayout.SOUTH);
	}
	
	private class TopBottomBorder implements Border {
		private final Border delegate;

		public TopBottomBorder(Border delegate) {
			this.delegate = delegate;
		}
		
		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			delegate.paintBorder(c, g, x, y, width, height);
		}

		@Override
		public Insets getBorderInsets(Component c) {
			Insets insetsOfDelegate = delegate.getBorderInsets(c);
			Insets insets = new Insets(insetsOfDelegate.top, 0, insetsOfDelegate.bottom, 0);
			return insets;
		}

		@Override
		public boolean isBorderOpaque() {
			return delegate.isBorderOpaque();
		}
		
	}
	
	// TODO accelerators for buttons
	private static void installAccelerator(Action action, final JButton button) {
		if (action.getValue(Action.ACCELERATOR_KEY) instanceof KeyStroke) {
			KeyStroke keyStroke = (KeyStroke)action.getValue(Action.ACCELERATOR_KEY);
			InputMap windowInputMap = SwingUtilities.getUIInputMap(button, JComponent.WHEN_IN_FOCUSED_WINDOW);
			if (windowInputMap == null) {
				windowInputMap = new ComponentInputMapUIResource(button);
				SwingUtilities.replaceUIInputMap(button, JComponent.WHEN_IN_FOCUSED_WINDOW, windowInputMap);
			}
			windowInputMap.put(keyStroke, keyStroke.toString());
			button.getActionMap().put(keyStroke.toString(), action);
		}
	}
	
	private static class ButtonBar extends JPanel {
		private static final long serialVersionUID = 1L;

		public ButtonBar(Action... actions) {
			super(new FlowLayout(FlowLayout.RIGHT, 5, 0)); // align, hgap, vgap
			setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 2)); // unknown: why the need for additional 2 pixel?

			for (Action action: actions) {
				add(new JButton(action));
			}
		}
	}

}
