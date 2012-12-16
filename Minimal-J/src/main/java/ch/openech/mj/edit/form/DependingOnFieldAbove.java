package ch.openech.mj.edit.form;


public interface DependingOnFieldAbove<T> {

	T getKeyOfDependedField();

	void valueChanged(T value);

}
