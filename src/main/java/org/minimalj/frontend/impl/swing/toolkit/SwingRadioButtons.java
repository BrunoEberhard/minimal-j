package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.Rendering;
import org.minimalj.util.StringUtils;

public class SwingRadioButtons<T> extends JPanel implements Input<T> {
	private static final long serialVersionUID = 1L;
	
	private final Map<T, ButtonModel> modelByObject = new LinkedHashMap<>();
	private final Map<ButtonModel, T> objectByModel = new LinkedHashMap<>();
	private final ButtonGroup buttonGroup = new ButtonGroup();
	
	public SwingRadioButtons(List<T> objects, InputComponentListener listener) {
		super(new FlowLayout());

		ItemListener buttonListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				listener.changed(SwingRadioButtons.this);
			}
		};
		
		for (T object : objects) {
			JRadioButton radioButton = new JRadioButton(Rendering.toString(object));
			String description = Rendering.toDescriptionString(object);
			if (!StringUtils.isEmpty(description)) {
				radioButton.setToolTipText(description);
			}
			radioButton.addItemListener(buttonListener);
			modelByObject.put(object, radioButton.getModel());
			objectByModel.put(radioButton.getModel(), object);
			add(radioButton);
			buttonGroup.add(radioButton);
		}
		setInheritsPopupMenu(true);
	}


	@Override
	public void setValue(T object) {
		buttonGroup.setSelected(modelByObject.get(object), true);
	}

	@Override
	public T getValue() {
		return objectByModel.get(buttonGroup.getSelection());
	}

	@Override
	public void setEditable(boolean enabled) {
		for (Component c : getComponents()) {
			((JRadioButton) c).setEnabled(enabled);
		}
	}
	
}

