package org.minimalj.frontend.vaadin.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.Input;
import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.VerticalLayout;

public class VaadinCheckBox extends VerticalLayout implements Input<Boolean> {

	private static final long serialVersionUID = 1L;
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
	public void setValue(Boolean selected) {
		boolean readOnly = checkBox.isReadOnly();
		checkBox.setReadOnly(false);
		checkBox.setValue(Boolean.TRUE.equals(selected));
		checkBox.setReadOnly(readOnly);
	}

	@Override
	public Boolean getValue() {
		return (Boolean) checkBox.getValue();
	}
	
	@Override
	public void setEditable(boolean editable) {
		checkBox.setReadOnly(!editable);
	}

	public class CheckBoxChangeListener implements CheckBox.ValueChangeListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
			listener.changed(VaadinCheckBox.this);
		}
	}
	
}
