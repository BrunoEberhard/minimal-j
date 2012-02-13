package ch.openech.mj.vaadin.toolkit;

import java.util.List;

import com.vaadin.ui.ListSelect;

import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.VisualList;

public class VaadinVisualList extends ListSelect implements VisualList {

	private List<?> objects;
	
	@Override
	public void requestFocus() {
		super.focus();
	}

	@Override
	public void setObjects(List<?> objects) {
		this.objects = objects;
		removeAllItems();
		for (Object item : objects) {
			addItem(item);
		}
	}

	@Override
	public void setSelectedObject(Object object) {
		setValue(object);
	}

	@Override
	public Object getSelectedObject() {
		return getValue();
	}

	@Override
	public int getSelectedIndex() {
		return objects.indexOf(getValue());
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		VaadinIndication.setValidationMessages(validationMessages, this);
	}

	@Override
	public void setClickListener(ClickListener clickListener) {
		// TODO ClickListener auf VaandinVisualList
	}

}
