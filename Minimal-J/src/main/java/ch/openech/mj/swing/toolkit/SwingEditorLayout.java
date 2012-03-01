package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentInputMapUIResource;

import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.util.StringUtils;

public class SwingEditorLayout extends JPanel implements IComponent {

	public SwingEditorLayout(String information, IComponent content, Action[] actions) {
		super(new BorderLayout());
		addInformation(information);
		JScrollPane scrollPane = new JScrollPane(new ScrollablePanel(SwingClientToolkit.getComponent(content)));
		scrollPane.setBorder(null);
		add(scrollPane, BorderLayout.CENTER);
		add(createButtonBar(actions), BorderLayout.SOUTH);
	}

	protected void addInformation(String information) {
		if (!StringUtils.isBlank(information)) {
			JLabel help = new JLabel(information);
			help.setBorder(BorderFactory.createEmptyBorder(7, 10, 10, 7));
			add(help, BorderLayout.NORTH);
		}
	}

	protected JPanel createButtonBar(Action... actions) {
		JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); // align, hgap, vgap
		buttonBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 2)); // unknown: why the need for additional 2 pixel?
		addButtons(buttonBar, actions);
		return buttonBar;
	}
	
	protected void addButtons(JPanel buttonBar, Action... actions) {
		for (Action action: actions) {
			addActionButton(buttonBar, action);
		}
	}

	protected void addActionButton(JPanel buttonBar, Action action) {
		JButton button = new JButton(action);
		buttonBar.add(button);
		installAccelerator(action);
		installAdditionalActionListener(action, button);
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

	protected void installAccelerator(Action action) {
		if (action.getValue(Action.ACCELERATOR_KEY) instanceof KeyStroke) {
			KeyStroke keyStroke = (KeyStroke)action.getValue(Action.ACCELERATOR_KEY);
			InputMap windowInputMap = SwingUtilities.getUIInputMap(this, JComponent.WHEN_IN_FOCUSED_WINDOW);
			if (windowInputMap == null) {
				windowInputMap = new ComponentInputMapUIResource(this);
				SwingUtilities.replaceUIInputMap(this, JComponent.WHEN_IN_FOCUSED_WINDOW, windowInputMap);
			}
			windowInputMap.put(keyStroke, keyStroke.toString());
			this.getActionMap().put(keyStroke.toString(), action);
		}
	}
	
    private static final class ScrollablePanel extends JPanel implements Scrollable {

    	public ScrollablePanel(Component content) {
    		super(new BorderLayout());
    		add(content, BorderLayout.CENTER);
    	}
    	
        @Override
    	public Dimension getPreferredScrollableViewportSize() {
        	return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
            return 30;
        }

        @Override
        public int getScrollableBlockIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
            return visibleRect.width;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
    		return true;
        }
        
        @Override
        public boolean getScrollableTracksViewportHeight() {
    		if (getParent() instanceof JViewport) {
    			return (((JViewport) getParent()).getHeight() > getPreferredSize().height);
    		}
    		return false;
        }

    }
}
