package org.minimalj.frontend.page;

import org.minimalj.model.View;
import org.minimalj.model.ViewUtil;
import org.minimalj.util.ClassHolder;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.GenericUtils;

/**
 * If the objects in the table have the same class as the edited objects you can
 * use SimpleTableEditorPage . This class should only be used if the objects
 * have a lot of (list) fields and the loading could get expensive. Normally the
 * SimpleTableEditorPage is enough.
 * 
 * @param <VIEW>
 *            the (View) class of the elements displayed in the table
 * @param <T>
 *            the class of the complete objects. This is used for editing.
 */
public abstract class TableEditorPage<VIEW extends View<T>, T> extends BaseTableEditorPage<VIEW, T> {

	private final ClassHolder<T> classT;

	public TableEditorPage() {
		this.classT = new ClassHolder<>((Class<T>) GenericUtils.getGenericClass(this.getClass(), 1));
	}
	
	@Override
	protected T createObject() {
		return CloneHelper.newInstance(classT.getClazz());
	}
	
	@Override
	protected T viewed(VIEW view) {
		return ViewUtil.viewed(view);
	}
	
	@Override
	protected VIEW view(T object) {
		return ViewUtil.view(object, CloneHelper.newInstance(getClazz()));
	}
	
}
