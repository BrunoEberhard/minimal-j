package org.minimalj.frontend.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.IAction;

public class SwingEditorLayout extends JPanel implements IComponent {
	private static final long serialVersionUID = 1L;

	public SwingEditorLayout(IContent content, IAction[] actions) {
		super(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(new ScrollablePanel((Component) content));
		scrollPane.setBorder(new TopBottomBorder(scrollPane.getBorder()));
		add(scrollPane, BorderLayout.CENTER);
		ButtonBar buttonBar = new ButtonBar(SwingClientToolkit.adaptActions(actions));
		buttonBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 2)); // unknown: why the need for additional 2 pixel?
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

	
	private static class ButtonBar extends JPanel {
		private static final long serialVersionUID = 1L;

		public ButtonBar(Action... actions) {
			super(new FlowLayout(FlowLayout.RIGHT, 5, 0)); // align, hgap, vgap
			addButtons(actions);
		}
		
		private void addButtons(Action... actions) {
			for (Action action: actions) {
				addActionButton(action);
			}
		}

		private void addActionButton(Action action) {
			JButton button = createButton(action);
			add(button);
		}

		public static JButton createButton(Action action) {
			JButton button = new SwingHeavyActionButton(action);
			installAccelerator(action, button);
			installAdditionalActionListener(action, button);
			return button;
		}

		private static void installAdditionalActionListener(Action action, final JButton button) {
			action.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if ("visible".equals(evt.getPropertyName()) && (evt.getNewValue() instanceof Boolean)) {
						button.setVisible((Boolean) evt.getNewValue());
					} else if ("foreground".equals(evt.getPropertyName()) && (evt.getNewValue() instanceof Color)) {
						button.setForeground((Color) evt.getNewValue());
					}
				}
			});
		}

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
		
	}

}
