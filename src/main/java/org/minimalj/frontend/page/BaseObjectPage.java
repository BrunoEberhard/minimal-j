package org.minimalj.frontend.page;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.util.CloneHelper;

public abstract class BaseObjectPage<T> extends Page {

	private T object;
	private transient Form<T> form;
	
	public BaseObjectPage(T object) {
		this.object = object;
	}

	public void setObject(T object) {
		this.object = object;
		if (form != null) {
			form.setObject(object);
		}
	}
	
	protected abstract Form<T> createForm();

	public T getObject() {
		return object;
	}
	
	@Override
	public IContent getContent() {
		if (form == null) {
			form = createForm();
		}
		form.setObject(getObject());
		return form.getContent();
	}
	
	public abstract class ObjectEditor extends Editor<T, T> {
		
		@Override
		protected T createObject() {
			return CloneHelper.clone(BaseObjectPage.this.getObject());
		}
		
		@Override
		protected void finished(T result) {
			setObject(result);
		}
	}

}
