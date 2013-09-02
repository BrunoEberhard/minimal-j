package ch.openech.mj.vaadin.toolkit;

import ch.openech.mj.toolkit.ClientToolkit.InputComponentListener;

import com.vaadin.ui.CheckBox;

public class VaadinCheckBox extends CheckBox implements ch.openech.mj.toolkit.CheckBox {

	private final InputComponentListener listener;
	
	public VaadinCheckBox(InputComponentListener listener, String text) {
		super(text);
		this.listener = listener;
		setImmediate(true);
		addListener(new CheckBoxChangeListener());
	}

	@Override
	public void setSelected(boolean selected) {
		super.setValue(selected);
	}

	@Override
	public boolean isSelected() {
		return Boolean.TRUE.equals(getValue());
	}

	public class CheckBoxChangeListener implements ValueChangeListener {

		@Override
		public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
			listener.changed(VaadinCheckBox.this);
		}
	}
	
}
