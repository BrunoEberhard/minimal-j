package ch.openech.mj.vaadin.toolkit;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.vaadin.ui.CheckBox;

public class VaadinCheckBox extends CheckBox implements ch.openech.mj.toolkit.CheckBox {

	private CheckBoxChangeListener changeListener;
	
	public VaadinCheckBox(String text) {
		super(text);
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

	@Override
	public void setChangeListener(ChangeListener listener) {
		if (changeListener == null) {
			changeListener = new CheckBoxChangeListener();
		}
		changeListener.setChangeListener(listener);
	}

	public class CheckBoxChangeListener implements ValueChangeListener {

		private ChangeListener changeListener;
		
		public void setChangeListener(ChangeListener changeListener) {
			if (changeListener == null) {
				if (this.changeListener != null) {
					removeListener(this);
				}
			} else {
				if (this.changeListener == null) {
					addListener(this);
				}
			}		
			this.changeListener = changeListener;
		}
		
		@Override
		public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
			changeListener.stateChanged(new ChangeEvent(VaadinCheckBox.this));
		}
	}
	
}
