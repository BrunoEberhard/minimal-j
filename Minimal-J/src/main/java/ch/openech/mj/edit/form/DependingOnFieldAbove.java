package ch.openech.mj.edit.form;

import ch.openech.mj.edit.fields.EditField;

public interface DependingOnFieldAbove<T> {

	String getNameOfDependedField();

	void setDependedField(EditField<T> field);

}
