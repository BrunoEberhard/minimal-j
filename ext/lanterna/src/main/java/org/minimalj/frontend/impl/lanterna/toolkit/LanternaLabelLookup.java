package org.minimalj.frontend.impl.lanterna.toolkit;

import org.minimalj.frontend.Frontend.Input;

import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.LinearLayout.Alignment;
import com.googlecode.lanterna.gui2.Panel;

public class LanternaLabelLookup extends Panel implements Input<String> {
	private final Label textField;
	private final Button lookupButton;

	public LanternaLabelLookup(Runnable runnable) {
		setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

		this.textField = new Label("");
		addComponent(textField, LinearLayout.createLayoutData(Alignment.Center));

		this.lookupButton = new Button("..", () -> LanternaFrontend.run(textField, runnable));
		lookupButton.setRenderer(new Button.FlatButtonRenderer());
		addComponent(lookupButton, LinearLayout.createLayoutData(Alignment.End));
	}

	@Override
	public void setValue(String value) {
		textField.setText(value);
	}

	@Override
	public String getValue() {
		return textField.getText();
	}
	
	@Override
	public void setEditable(boolean editable) {
		lookupButton.setEnabled(editable);
	}
}
