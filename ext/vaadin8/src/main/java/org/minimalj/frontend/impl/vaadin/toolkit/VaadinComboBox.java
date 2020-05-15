package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.Rendering;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ItemCaptionGenerator;

public class VaadinComboBox<T> extends ComboBox<T> implements Input<T> {
	private static final long serialVersionUID = 1L;
	
	private final List<T> objects;
	private T setObject;
	
	public VaadinComboBox(List<T> objects, InputComponentListener listener) {
		// setNullSelectionAllowed(true);
		this.objects = objects;
		updateChoice();
		addValueChangeListener(event -> listener.changed(VaadinComboBox.this));
		
		setItemCaptionGenerator(new RenderingItemCaptionGenerator());
	}

	private void updateChoice() {
		if (setObject != null && !objects.contains(setObject)) {
			List<T> objects2 = new ArrayList<T>(objects.size() + 1);
			objects2.addAll(objects);
			objects.add(setObject);
			setItems(objects2);
		} else {
			setItems(objects);
		}
	}

//		Tooltip not yet supported. Maybe easy with ItemDescriptionGenerator
	private class RenderingItemCaptionGenerator implements ItemCaptionGenerator<T> {
		private static final long serialVersionUID = 1L;

		@Override
		public String apply(T item) {
			return Rendering.toString(item);
		}
	}
	
	@Override
	public void setValue(T object) {
		if (setObject != null && !setObject.equals(object) || setObject == null && object != null) {
			this.setObject = (T) object;
			updateChoice();
		}
		super.setValue(object);
	}

	@Override
	public void setEditable(boolean editable) {
		super.setEnabled(editable);
	}

}
