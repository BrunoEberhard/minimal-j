package org.minimalj.frontend.page;

import org.minimalj.model.View;
import org.minimalj.model.ViewUtil;
import org.minimalj.util.ClassHolder;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.GenericUtils;

public abstract class TableEditorPage<VIEW extends View<T>, T> extends BaseTableEditorPage<VIEW, T> {

	private final ClassHolder<T> classT;

	@SuppressWarnings("unchecked")
	public TableEditorPage() {
		super();
		this.classT = new ClassHolder<>((Class<T>) GenericUtils.getGenericClass(this.getClass(), 1));
	}

	@SuppressWarnings("unchecked")
	public TableEditorPage(Object[] columns) {
		super(columns);
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
		return ViewUtil.view(object, CloneHelper.newInstance(clazz.getClazz()));
	}
	
}
