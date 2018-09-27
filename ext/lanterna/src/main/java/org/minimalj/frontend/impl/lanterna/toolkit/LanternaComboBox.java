package org.minimalj.frontend.impl.lanterna.toolkit;

import java.util.List;
import java.util.stream.Collectors;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.Rendering;
import org.minimalj.util.EqualsHelper;

import com.googlecode.lanterna.gui2.ComboBox;

public class LanternaComboBox<T> extends ComboBox<CharSequence> implements Input<T> {

	private final List<T> objects;
	private final InputComponentListener changeListener;
	
	public LanternaComboBox(List<T> objects, InputComponentListener changeListener) {
		super(render(objects), -1);
		this.objects = objects;
		this.changeListener = changeListener;
		addListener((from, to) -> fireChangeEvent());
	}

	private static List<CharSequence> render(List<?> objects) {
		return objects.stream().map(Rendering::render).collect(Collectors.toList());
	}

	private void fireChangeEvent() {
		changeListener.changed(LanternaComboBox.this);
	}

	@Override
	public void setEditable(boolean editable) {
		super.setReadOnly(!editable);
	}

	@Override
	public void setValue(T value) {
		for (int i = 0; i<getItemCount(); i++) {
			if (EqualsHelper.equals(getItem(i), value)) {
				setSelectedIndex(i);
				return;
			}
		}
		setSelectedIndex(-1);
	}

	@Override
	public T getValue() {
		return objects.get(getSelectedIndex());
	}
}
