package ch.openech.mj.vaadin.toolkit;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.vaadin.ui.CheckBox;

public class VaadinCheckBox extends CheckBox implements ch.openech.mj.toolkit.CheckBox {

	private final ChangeListener listener;
	
	public VaadinCheckBox(ChangeListener listener, String text) {
		super(text);
		this.listener = listener;
		addListener(new CheckBoxChangeListener());
	}

	@Override
	public void requestFocus() {
		focus();
	}

	@Override
	public void setSelected(boolean selected) {
		super.setValue(true);
	}

	@Override
	public boolean isSelected() {
		return Boolean.TRUE.equals(getValue());
	}

	public class CheckBoxChangeListener implements ValueChangeListener {

		@Override
		public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
			listener.stateChanged(new ChangeEvent(VaadinCheckBox.this));
		}
	}
	
}
