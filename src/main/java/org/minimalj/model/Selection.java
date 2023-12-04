package org.minimalj.model;

import java.util.Collections;
import java.util.List;

public class Selection<T> implements Rendering {

	public final T selectedValue;
	public final List<T> values;

	public Selection() {
		this.selectedValue = null;
		this.values = null;
	}

	public Selection(T selectedValue) {
		this.selectedValue = selectedValue;
		this.values = null;
	}

	public Selection(T selectedValue, List<T> values) {
		this.values = values != null ? Collections.unmodifiableList(values) : null;
		this.selectedValue = selectedValue;
	}

	@Override
	public CharSequence render() {
		return Rendering.render(selectedValue);
	}

	@Override
	public CharSequence renderDescription() {
		return selectedValue instanceof Rendering ? ((Rendering) selectedValue).renderDescription() : null;
	}

}
