package ch.openech.mj.lanterna.component;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;

public class TextMenu extends Panel {

	private String indent = "";
	
	public TextMenu() {
		super(Orientation.VERTICAL);
	}
	
	public void addItem(String text) {
		super.addComponent(new Label(indent + text));
	}
	
	public void addAction(String text, Action action) {
		super.addComponent(new Button(indent + text, action));
	}
	
	public void indent() {
		indent += "  ";
	}
	
	public void removeIndent() {
		indent = indent.substring(2);
	}
	
}
