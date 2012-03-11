package ch.openech.mj.edit.form;

import ch.openech.mj.edit.fields.AbstractEditField;
import ch.openech.mj.edit.fields.EditField;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.IComponentDelegate;

public abstract class DependenceDecorator<T> extends AbstractEditField<T> implements DependingOnFieldAbove<T>, Indicator {

	private final EditField<T> field;
	private final String nameOfDependedField;
	
	public DependenceDecorator(EditField<T> field, String nameOfDependedField) {
		super(field.getName() + DependenceDecorator.class.getSimpleName());
		this.field = field;
		this.nameOfDependedField = nameOfDependedField;
	}
	
	@Override
	public IComponent getComponent0() {
		if (field instanceof IComponentDelegate) {
			return (IComponent) ((IComponentDelegate) field).getComponent();
		} else {
			return field;
		}
	}

	@Override
	public String getNameOfDependedField() {
		return nameOfDependedField;
	}

	@Override
	public abstract void setDependedField(EditField<T> field);
	
	@Override
	protected Indicator[] getIndicatingComponents() {
		if (field instanceof Indicator) {
			Indicator indicator = (Indicator) field;
			return new Indicator[]{indicator};
		} else {
			return new Indicator[0];
		}
	}

	@Override
	public T getObject() {
		return field.getObject();
	}

	@Override
	public void setObject(T object) {
		field.setObject(object);
	}
}
