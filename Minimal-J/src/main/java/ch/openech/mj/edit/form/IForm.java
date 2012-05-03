package ch.openech.mj.edit.form;

import javax.swing.Action;

import ch.openech.mj.edit.ChangeableValue;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.Validatable;
import ch.openech.mj.toolkit.IComponent;

public interface IForm<T> extends ChangeableValue<T>, Validatable, Indicator {

	public IComponent getComponent();
	
	public abstract boolean isResizable();
	
	public void setSaveAction(Action saveAction);

}