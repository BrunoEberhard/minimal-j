package ch.openech.mj.edit.form;

import javax.swing.Action;

import ch.openech.mj.edit.ChangeableValue;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.Validatable;
import ch.openech.mj.toolkit.IComponentDelegate;

public interface IForm<T> extends IComponentDelegate, ChangeableValue<T>, Validatable, Indicator {

	public abstract boolean isResizable();
	
	public void setSaveAction(Action saveAction);

}