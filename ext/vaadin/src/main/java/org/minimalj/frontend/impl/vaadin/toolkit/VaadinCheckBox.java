package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;

import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.VerticalLayout;

public class VaadinCheckBox extends VerticalLayout implements Input<Boolean> {

	private static final long serialVersionUID = 1L;
	private final InputComponentListener listener;
	private final CheckBox checkBox;
	
	public VaadinCheckBox(InputComponentListener listener, String text) {
		checkBox = new CheckBox(text);
		addComponent(checkBox);
		this.listener = listener;
		checkBox.addListener(new CheckBoxChangeListener());
	}

	@Override
	public void setValue(Boolean selected) {
		boolean readOnly = checkBox.isReadOnly();
		checkBox.setReadOnly(false);
		checkBox.setValue(selected);
		checkBox.setReadOnly(readOnly);
	}

	@Override
	public Boolean getValue() {
		return checkBox.getValue();
	}
	
	@Override
	public void setEditable(boolean editable) {
		checkBox.setReadOnly(!editable);
	}

	public class CheckBoxChangeListener implements CheckBox.ValueChangeListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void valueChange(com.vaadin.v7.data.Property.ValueChangeEvent event) {
			listener.changed(VaadinCheckBox.this);
		}
	}
	
}
