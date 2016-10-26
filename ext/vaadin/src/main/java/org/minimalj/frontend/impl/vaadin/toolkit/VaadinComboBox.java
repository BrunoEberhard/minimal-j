package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.List;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;

import com.vaadin.ui.ComboBox;

public class VaadinComboBox<T> extends ComboBox implements Input<T> {
	private static final long serialVersionUID = 1L;
	
	private final InputComponentListener listener;
	private final List<T> objects;
	private T setObject;
	
	public VaadinComboBox(List<T> objects, InputComponentListener listener) {
		setNullSelectionAllowed(true);
		setImmediate(true);
		this.listener = listener;
		this.objects = objects;
		updateChoice();
		addValueChangeListener(new ComboBoxChangeListener());
	}

	private void updateChoice() {
		removeAllItems();
		if (setObject != null && !objects.contains(setObject)) {
			add(setObject);
		}
		for (Object object : objects) {
			add(object);
		}
	}

	public void add(Object item) {
		super.addItem(item);
		if (item instanceof Rendering) {
			Rendering renderingValue = (Rendering) item;
			String text = renderingValue.render(RenderType.PLAIN_TEXT);
			setItemCaption(item, text);
// 			Tooltip not yet supported. Maybe easy with ItemDescriptionGenerator
//			String tooltip = renderingValue.renderTooltip(RenderType.PLAIN_TEXT);
//			if (tooltip != null) {
//			}
		}

	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void setValue(Object object) {
		if (setObject != null && !setObject.equals(object) || setObject == null && object != null) {
			this.setObject = (T) object;
			updateChoice();
		}
		super.setValue(object);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T getValue() {
		return (T) super.getValue();
	}
	
	@Override
	public void setEditable(boolean editable) {
		super.setEnabled(editable);
	}

	public class ComboBoxChangeListener implements ValueChangeListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
			listener.changed(VaadinComboBox.this);
		}
	}

}
