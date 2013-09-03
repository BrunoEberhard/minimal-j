package ch.openech.mj.vaadin.toolkit;

import ch.openech.mj.toolkit.ClientToolkit.InputComponentListener;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.VerticalLayout;

public class VaadinCheckBox extends VerticalLayout implements ch.openech.mj.toolkit.CheckBox {

	private final InputComponentListener listener;
	private final CheckBox checkBox;
	
	public VaadinCheckBox(InputComponentListener listener, String text) {
		checkBox = new CheckBox(text);
		addComponent(checkBox);
		this.listener = listener;
		setImmediate(true);
		checkBox.addListener(new CheckBoxChangeListener());
	}

	@Override
	public void setSelected(boolean selected) {
		boolean readOnly = checkBox.isReadOnly();
		checkBox.setReadOnly(false);
		checkBox.setValue(selected);
		checkBox.setReadOnly(readOnly);
	}

	@Override
	public boolean isSelected() {
		return Boolean.TRUE.equals(checkBox.getValue());
	}
	
	@Override
	public void setEditable(boolean editable) {
		checkBox.setReadOnly(!editable);
	}

	public class CheckBoxChangeListener implements CheckBox.ValueChangeListener {

		@Override
		public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
			listener.changed(VaadinCheckBox.this);
		}
	}
	
}
