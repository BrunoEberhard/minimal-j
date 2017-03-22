package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;

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

	private class RenderingItemCaptionGenerator implements ItemCaptionGenerator<T> {
		private static final long serialVersionUID = 1L;

		@Override
		public String apply(T item) {
			if (item instanceof Rendering) {
				Rendering renderingValue = (Rendering) item;
				return renderingValue.render(RenderType.PLAIN_TEXT);
//	 			Tooltip not yet supported. Maybe easy with ItemDescriptionGenerator
//				String tooltip = renderingValue.renderTooltip(RenderType.PLAIN_TEXT);
//				if (tooltip != null) {
//				}
			} else if (item != null) {
				return item.toString();
			} else {
				return null;
			}
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
