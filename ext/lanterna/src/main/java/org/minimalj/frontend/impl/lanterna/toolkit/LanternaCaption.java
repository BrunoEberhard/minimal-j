package org.minimalj.frontend.impl.lanterna.toolkit;

import java.util.List;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.CheckBox;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LayoutData;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.LinearLayout.Alignment;
import com.googlecode.lanterna.gui2.Panel;

public class LanternaCaption extends Panel {

	private static final LayoutData FILL = LinearLayout.createLayoutData(Alignment.Fill);

	private Label validationLabel;
	
	public LanternaCaption(Component component, String caption) {
		super(new LinearLayout());
		Panel panel = new Panel(new LinearLayout(Direction.HORIZONTAL));
		panel.addComponent(new Label(caption));
		
		validationLabel = new Label("");
		validationLabel.setForegroundColor(TextColor.ANSI.RED);
		panel.addComponent(validationLabel);
		
		addComponent(panel);
		if (component instanceof CheckBox) {
			addComponent(component);
		} else {
			addComponent(component, FILL);
		}
	}
	
	public void setValidationMessages(List<String> validationMessages) {
		String text = validationMessages.isEmpty() ? "" : "*";
		if (!text.equals(validationLabel.getText())) {
			validationLabel.setText(text);
		}
	}

}
