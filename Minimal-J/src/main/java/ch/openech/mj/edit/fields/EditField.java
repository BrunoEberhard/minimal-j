package ch.openech.mj.edit.fields;

import ch.openech.mj.edit.ChangeableValue;

public interface EditField<T> extends FormField<T>, ChangeableValue<T> {

	public boolean isEmpty();
	
}
