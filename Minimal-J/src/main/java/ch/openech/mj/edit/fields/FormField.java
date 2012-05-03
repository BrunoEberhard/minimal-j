package ch.openech.mj.edit.fields;

import ch.openech.mj.edit.Value;
import ch.openech.mj.toolkit.IComponent;

public interface FormField<T> extends Value<T> {

	public IComponent getComponent();
	
	public String getName();

}