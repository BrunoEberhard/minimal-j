package org.minimalj.frontend.page;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.IdUtils;
import org.minimalj.util.resources.Resources;

abstract class BaseTableEditorPage<VIEW, T> extends TableDetailPage<VIEW> {

	protected final boolean hasDetailPage;

	private DetailPage detailPage;

	protected BaseTableEditorPage() {
		this(false);
	}

	protected BaseTableEditorPage(boolean hasDetailPage) {
		super();
		this.hasDetailPage = hasDetailPage;
	}

	protected abstract Form<T> createForm(boolean editable, boolean newObject);
	
	protected void validate(T object, boolean newObject, List<ValidationMessage> validationMessages) {
		// to be overridden
	}
	
	protected T save(T editedObject, T originalObject) {
		return save(editedObject);
	}
	
	protected T save(T object) {
		// TODO only with ID
		return Backend.save(object);
	}
	
	@Override
	public List<Action> getTableActions() {
		return Arrays.asList(new TableNewObjectEditor(), new TableEditor(), new DeleteDetailAction());
	}
	
	@Override
	public void action(VIEW selectedView) {
		if (hasDetailPage) {
			super.action(selectedView);
		} else {
			T selectedObject = viewed(selectedView);
			openEditor(selectedObject);
		}
	}
	
	protected abstract T createObject();
	
	protected abstract T viewed(VIEW view);

	protected abstract VIEW view(T object);
	
	protected void openEditor(T selectedObject) {
		new TableEditor(selectedObject).run();
	}
	
	@Override
	protected Page getDetailPage(VIEW view) {
		T object = viewed(view);
		if (detailPage != null) {
			detailPage.setObject(object);
		} else {
			detailPage = new DetailPage(object);
		}
		return detailPage;
	}

	public class DetailPage extends ObjectPage<T> {

		public DetailPage(T detail) {
			super(detail);
	  	}
		
		@Override
		public String getTitle() {
			String title = Resources.getStringOrNull(getClass());
			if (title != null) {
				return title;
			} else {
				return MessageFormat.format(Resources.getString(DetailPage.class.getSimpleName() + ".title"), BaseTableEditorPage.this.getNameArguments());
			}
		}
		
		@Override
		public List<Action> getActions() {
			return Arrays.asList(new DetailPageEditor());
		}
		
		@Override
		protected Form<T> createForm() {
			return BaseTableEditorPage.this.createForm(Form.READ_ONLY, false);
		}
		
		public class DetailPageEditor extends ObjectPage<T>.ObjectEditor {

			@Override
			protected Object[] getNameArguments() {
				return BaseTableEditorPage.this.getNameArguments();
			}
			
			@Override
			protected Form<T> createForm() {
				return BaseTableEditorPage.this.createForm(Form.EDITABLE, false);
			}
			
			@Override
			protected void validate(T object, List<ValidationMessage> validationMessages) {
				BaseTableEditorPage.this.validate(object, false, validationMessages);
			}

			@Override
			protected T save(T object) {
				return BaseTableEditorPage.this.save(object, DetailPageEditor.this.getObject());
			}
			
			@Override
			protected void finished(T result) {
				super.finished(result);
				BaseTableEditorPage.this.refresh();
			}
		}
	}
	
	protected abstract class AbstractTableEditor extends SimpleEditor<T>  {
		
		@Override
		protected Object[] getNameArguments() {
			return BaseTableEditorPage.this.getNameArguments();
		}
		
		@Override
		protected Form<T> createForm() {
			return BaseTableEditorPage.this.createForm(Form.EDITABLE, false);
		}
		
		@Override
		protected void validate(T object, List<ValidationMessage> validationMessages) {
			BaseTableEditorPage.this.validate(object, false, validationMessages);
		}
		
		@Override
		protected T save(T object) {
			return BaseTableEditorPage.this.save(object);
		}
		
		@Override
		protected void finished(T result) {
			BaseTableEditorPage.this.refresh();
			VIEW view = view(result);
			BaseTableEditorPage.this.selectionChanged(Arrays.asList(view));
		}

		@Override
		public int getWidth() {
			return BaseTableEditorPage.this.getEditorWidth();
		}

		@Override
		public int getHeight() {
			return BaseTableEditorPage.this.getEditorHeight();
		}
	}	
	
	public int getEditorWidth() {
		return FIT_CONTENT;
	}

	public int getEditorHeight() {
		return FIT_CONTENT;
	}

	public class TableEditor extends AbstractTableEditor implements TableSelectionAction<T> {
		private T selection;

		public TableEditor() {
			selectionChanged(null);
		}
		
		public TableEditor(T selectedObject) {
			this.selection = selectedObject;
			setEnabled(selection != null);
		}

		@Override
		protected Object[] getNameArguments() {
			return BaseTableEditorPage.this.getNameArguments();
		}
		
		protected T getSelection() {
			return selection;
		}
		
		@Override
		protected T createObject() {
			return IdUtils.hasId(getClazz()) ? CloneHelper.clone(selection) : selection;
		}
		
		@Override
		protected T save(T object) {
			return BaseTableEditorPage.this.save(object, selection);
		}
		
		@Override
		public void selectionChanged(List<T> selectedObjects) {
			this.selection = selectedObjects != null && !selectedObjects.isEmpty() ? selectedObjects.get(0) : null;
			setEnabled(selection != null);
		}
	}	
	
	public class TableNewObjectEditor extends AbstractTableEditor {

		@Override
		protected Object[] getNameArguments() {
			return BaseTableEditorPage.this.getNameArguments();
		}
		
		@Override
		protected Form<T> createForm() {
			return BaseTableEditorPage.this.createForm(Form.EDITABLE, true);
		}
		
		@Override
		protected T createObject() {
			return BaseTableEditorPage.this.createObject();
		}
		
		@Override
		protected void validate(T object, List<ValidationMessage> validationMessages) {
			BaseTableEditorPage.this.validate(object, true, validationMessages); // true -> new object
		}
	}
}
