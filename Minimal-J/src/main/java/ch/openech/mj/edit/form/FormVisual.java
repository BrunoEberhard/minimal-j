package ch.openech.mj.edit.form;

import javax.swing.Action;

import ch.openech.mj.edit.ChangeableValue;
import ch.openech.mj.edit.fields.Visual;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.Validatable;

public interface FormVisual<T> extends Visual, ChangeableValue<T>, Validatable, Indicator {

	public abstract boolean isResizable();
	
	public void setSaveAction(Action saveAction);

}