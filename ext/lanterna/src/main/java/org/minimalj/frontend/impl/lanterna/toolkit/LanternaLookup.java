package org.minimalj.frontend.impl.lanterna.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;

import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.LinearLayout.Alignment;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox.Style;

public class LanternaLookup extends Panel implements Input<String> {
	private final LanternaTextField textField;
	private final Button lookupButton;

	public LanternaLookup(Runnable runnable, InputComponentListener changeListener) {
		setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

		this.textField = new LanternaTextField(changeListener, Style.SINGLE_LINE);
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
		textField.setEditable(editable);
		lookupButton.setEnabled(editable);
	}
}
