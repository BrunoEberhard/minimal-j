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
import org.minimalj.util.resources.Resources;

public abstract class TableEditorPage<T> extends TableDetailPage<T, TableEditorPage<T>.DetailPage> {

	// generic mystery: how to do this static without warning
	public final TableEditorPage<T>.DetailPage NO_DETAIL_PAGE = null;
	
	public TableEditorPage() {
		super();
	}

	public TableEditorPage(Object[] columns) {
		super(columns);
	}
	
	protected abstract Form<T> createForm(boolean editable, boolean newObject);
	
	protected void validate(T object, boolean newObject, List<ValidationMessage> validationMessages) {
		// to be overridden
	}
	
	@Override
	public List<Action> getTableActions() {
		return Arrays.asList(new TableNewObjectEditor(), new TableEditor(), new DeleteDetailAction());
	}
	
	@Override
	public void action(T selectedObject) {
		boolean detailVisibleBefore = isDetailVisible();
		super.action(selectedObject);
		if (detailVisibleBefore || !isDetailVisible()) {
			openEditor(selectedObject);
		}
	}
	
	protected void openEditor(T selectedObject) {
		new TableEditor(selectedObject).action();
	}
	
	@Override
	protected DetailPage createDetailPage(T detail) {
		return new DetailPage(detail);
	}
	
	@Override
	protected DetailPage updateDetailPage(TableEditorPage<T>.DetailPage detailPage, T mainObject) {
		detailPage.setObject(mainObject);
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
				return MessageFormat.format(Resources.getString(DetailPage.class.getSimpleName() + ".title"), nameArguments);
			}
		}
		
		@Override
		public List<Action> getActions() {
			return Arrays.asList(new DetailPageEditor());
		}
		
		@Override
		protected Form<T> createForm() {
			return TableEditorPage.this.createForm(Form.READ_ONLY, false);
		}
		
		public class DetailPageEditor extends ObjectPage<T>.ObjectEditor {

			@Override
			protected Object[] getNameArguments() {
				return TableEditorPage.this.nameArguments;
			}
			
			@Override
			protected Form<T> createForm() {
				return TableEditorPage.this.createForm(Form.EDITABLE, false);
			}
			
			@Override
			protected void validate(T object, List<ValidationMessage> validationMessages) {
				TableEditorPage.this.validate(object, false, validationMessages);
			}

			@Override
			protected T save(T object) {
				return Backend.save(object);
			}
			
			@Override
			protected void finished(T result) {
				super.finished(result);
				TableEditorPage.this.refresh();
			}
		}
	}
	
	protected abstract class AbstractTableEditor extends SimpleEditor<T>  {
		
		@Override
		protected Object[] getNameArguments() {
			return nameArguments;
		}
		
		@Override
		protected Form<T> createForm() {
			return TableEditorPage.this.createForm(Form.EDITABLE, false);
		}
		
		@Override
		protected void validate(T object, List<ValidationMessage> validationMessages) {
			TableEditorPage.this.validate(object, false, validationMessages);
		}
		
		@Override
		protected T save(T object) {
			return Backend.save(object);
		}
		
		@Override
		protected void finished(T result) {
			TableEditorPage.this.refresh();
			TableEditorPage.this.selectionChanged(Arrays.asList(result));
		}
	}	
	
	public class TableEditor extends AbstractTableEditor implements TableSelectionAction<T> {
		private transient T selection;

		public TableEditor() {
			selectionChanged(null);
		}
		
		public TableEditor(T selectedObject) {
			this.selection = selectedObject;
			setEnabled(selection != null);
		}

		@Override
		protected T createObject() {
			return selection;
		}
		
		@Override
		public void selectionChanged(List<T> selectedObjects) {
			this.selection = selectedObjects != null && !selectedObjects.isEmpty() ? selectedObjects.get(0) : null;
			setEnabled(selection != null);
		}
	}	
	
	public class TableNewObjectEditor extends AbstractTableEditor {

		@Override
		protected Form<T> createForm() {
			return TableEditorPage.this.createForm(Form.EDITABLE, true);
		}
		
		@Override
		protected T createObject() {
			return CloneHelper.newInstance(clazz.getClazz());
		}
		
		@Override
		protected void validate(T object, List<ValidationMessage> validationMessages) {
			TableEditorPage.this.validate(object, true, validationMessages); // true -> new object
		}
	}

}
