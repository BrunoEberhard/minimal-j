package ch.openech.mj.edit;

import ch.openech.mj.edit.form.FormVisual;
import ch.openech.mj.page.Page;


public class DisplayPage<T> extends Page {
	private final FormVisual<T> form;
	
	public DisplayPage(FormVisual<T> form, T object) {
		super();
		form.setObject(object);
		this.form = form;
	}

	@Override
	public Object getPanel() {
		return form;
	}
	
}
