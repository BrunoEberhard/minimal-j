package org.minimalj.frontend.impl.lanterna.toolkit;

import org.minimalj.frontend.Frontend.Input;

import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Panel;

public class LanternaLookup extends Panel implements Input<String> {
	private final Input<String> stringInput;
	private final Button lookupButton;

	public LanternaLookup(Input<String> stringInput, Runnable lookup) {
		super(new BorderLayout());
		this.stringInput = stringInput;

		addComponent((Component) stringInput, Location.CENTER);

		this.lookupButton = new Button("..", () -> LanternaFrontend.run((Component) stringInput, lookup));
		lookupButton.setRenderer(new Button.FlatButtonRenderer());
		addComponent(lookupButton, Location.RIGHT);
	}

	@Override
	public void setValue(String value) {
		stringInput.setValue(value);
	}

	@Override
	public String getValue() {
		return stringInput.getValue();
	}
	
	@Override
	public void setEditable(boolean editable) {
		stringInput.setEditable(editable);
		lookupButton.setEnabled(editable);
	}
}
