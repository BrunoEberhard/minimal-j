package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import ch.openech.mj.toolkit.IAction;
import ch.openech.mj.toolkit.IComponent;

public class SwingEditorLayout extends JPanel implements IComponent {
	private static final long serialVersionUID = 1L;

	public SwingEditorLayout(IComponent content, IAction[] actions) {
		super(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(new ScrollablePanel((Component) content));
		scrollPane.setBorder(new TopBottomBorder(scrollPane.getBorder()));
		add(scrollPane, BorderLayout.CENTER);
		ButtonBar buttonBar = new ButtonBar(SwingClientToolkit.adaptActions(actions, this));
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
	
}
