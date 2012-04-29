package ch.openech.mj.swing.toolkit;

import java.awt.Color;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentInputMapUIResource;

public class ButtonBar extends JPanel {

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
		JButton button = new JButton(action);
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
