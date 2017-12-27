package org.minimalj.frontend.page;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.util.ClassHolder;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.resources.Resources;

/**
 * Shows a table of objects of one class. 
 *
 * @param <T> Class of objects in this TablePage. Must be specified.
 */
public abstract class TablePage<T> extends Page implements TableActionListener<T> {

	private final boolean multiSelect;
	private final Object[] keys;
	private final ClassHolder<T> clazz;
	private transient ITable<T> table;
	private transient List<T> objects;
	private transient List<TableSelectionAction<T>> actions;
	private transient List<T> selectedObjects;
	protected final Object[] nameArguments;
	
	/*
	 * this flag indicates if the next call of getContent should trigger a new loading
	 * of the data. A second call of getContent probably means that the user revisits
	 * the page and doesn't want to see the old data. 
	 */
	private transient boolean reloadFlag;
	
	@SuppressWarnings("unchecked")
	public TablePage(Object[] keys) {
		this.multiSelect = allowMultiselect();
		this.keys = keys;

		this.clazz = new ClassHolder<T>((Class<T>) GenericUtils.getGenericClass(getClass()));
		this.nameArguments = new Object[] { Resources.getString(Resources.getResourceName(clazz.getClazz())) };
	}

	/**
	 * To create a table with multiselection override this method an return true.
	 * This method is called once at construction time of the TablePage and must
	 * be constant for a class. It must not depend on the displayed values.
	 * @return true if table should allow multi selection
	 */
	protected boolean allowMultiselect() {
		return false;
	}
	
	protected abstract List<T> load();

	@Override
	public String getTitle() {
		String title = Resources.getStringOrNull(getClass());
		if (title != null) {
			return title;
		} else {
			String className = Resources.getString(clazz.getClazz());
			return MessageFormat.format(Resources.getString(TablePage.class.getSimpleName() + ".title"), className);
		}
	}
	
	@Override
	public IContent getContent() {
		table = Frontend.getInstance().createTable(keys, multiSelect, this);
		if (objects == null || reloadFlag) {
			objects = load();
			reloadFlag = true;
		}
		table.setObjects(objects);
		// for hidden/reshown detail it can happen that getContent is called
		// for a second time. Then the selection has to be cleared not to keep the old selection
		// (or table could be reused but than every Frontend has to take care about selection state)
		selectedObjects = null;
		return table;
	}
	
	public void refresh() {
		if (table != null) {
			objects = load();
			table.setObjects(objects);
			reloadFlag = false;
		}
	}
	
	@Override
	public void selectionChanged(List<T> selectedObjects) {
		this.selectedObjects = selectedObjects;
		if (actions != null) {
			actions.stream().forEach(action -> action.selectionChanged(selectedObjects));
		}
	}
	
	public abstract class DetailEditor extends SimpleEditor<T> implements TableSelectionAction<T> {
		protected T selection;

		protected DetailEditor() {
			registerSelectionAction(this);
			setEnabled(false);
		}
		
		@Override
		protected Object[] getNameArguments() {
			return nameArguments;
		}
		
		@Override
		protected T createObject() {
			return selection;
		}
		
		@Override
		public void selectionChanged(List<T> selectedObjects) {
			this.selection = selectedObjects.isEmpty() ? null : selectedObjects.get(0);
			setEnabled(selection != null);
		}
		
		@Override
		protected T save(T object) {
			return Backend.save(object);
		}
		
		@Override
		protected void finished(T result) {
			TablePage.this.refresh();
		}
	}	
	
	public abstract class NewDetailEditor extends SimpleEditor<T> {
		
		@Override
		protected Object[] getNameArguments() {
			return nameArguments;
		}
		
		@Override
		protected T createObject() {
			T newInstance = CloneHelper.newInstance(clazz.getClazz());
			return newInstance;
		}
		
		@Override
		protected void finished(T result) {
			TablePage.this.refresh();
		}
		
		@Override
		protected T save(T object) {
			return (T) Backend.save(object);
		}
	}
	
	public class DeleteDetailAction extends Action implements TableSelectionAction<T> {

		public DeleteDetailAction() {
			registerSelectionAction(this);
			selectionChanged(selectedObjects);
		}
		
		@Override
		protected Object[] getNameArguments() {
			return nameArguments;
		}
		
		@Override
		public void action() {
			for (T object : TablePage.this.selectedObjects) {
				Backend.delete(object.getClass(), IdUtils.getId(object));
			}
			TablePage.this.refresh();
		}

		@Override
		public void selectionChanged(List<T> selectedObjects) {
			setEnabled(selectedObjects != null && !selectedObjects.isEmpty());
		}
	}	
	
	protected void registerSelectionAction(TableSelectionAction<T> action) {
		if (actions == null) {
			actions = new ArrayList<>();
		}
		actions.add(action);
	}

	public interface TableSelectionAction<T> {
		
		public abstract void selectionChanged(List<T> selectedObjects);

	}

	public static abstract class TablePageWithDetail<T, DETAIL_PAGE extends Page> extends TablePage<T> {
		
		private DETAIL_PAGE detailPage;

		public TablePageWithDetail(Object[] keys) {
			super(keys);
		}

		protected abstract DETAIL_PAGE createDetailPage(T mainObject);

		protected abstract DETAIL_PAGE updateDetailPage(DETAIL_PAGE page, T mainObject);

		protected DETAIL_PAGE updateDetailPage(DETAIL_PAGE page, List<T> selectedObjects) {
			if (selectedObjects == null || selectedObjects.size() != 1) {
				return null;
			} else {
				return updateDetailPage(page, selectedObjects.get(0));
			}
		}
		
		@Override
		public void action(T selectedObject) {
			if (detailPage != null) {
				updateDetailPage(Collections.singletonList(selectedObject));
			} else {
				detailPage = createDetailPage(selectedObject);
				if (detailPage != null) {
					Frontend.showDetail(TablePageWithDetail.this, detailPage);
				}
			}
		}

		@Override
		public void selectionChanged(List<T> selectedObjects) {
			super.selectionChanged(selectedObjects);
			boolean detailVisible = detailPage != null && Frontend.isDetailShown(detailPage); 
			if (detailVisible) {
				if (selectedObjects != null && !selectedObjects.isEmpty()) {
					updateDetailPage(selectedObjects);
				} else {
					Frontend.hideDetail(detailPage);
				}
			}
		}
		
		private void updateDetailPage(List<T> selectedObjects) {
			DETAIL_PAGE updatedDetailPage = updateDetailPage(detailPage, selectedObjects);
			if (Frontend.isDetailShown(detailPage)) {
				if (updatedDetailPage == null || updatedDetailPage != detailPage) {
					Frontend.hideDetail(detailPage);
				}
			}
			if (updatedDetailPage != null) {
				Frontend.showDetail(TablePageWithDetail.this, updatedDetailPage);
				detailPage = updatedDetailPage;
			}
		}
	}
	
	public static abstract class SimpleTablePageWithDetail<T> extends TablePageWithDetail<T, SimpleTablePageWithDetail<T>.DetailPage> {

		public SimpleTablePageWithDetail(Object[] keys) {
			super(keys);
		}
		
		protected abstract Form<T> createForm(boolean editable);
		
		public class DetailEditor extends TablePage<T>.DetailEditor {

			@Override
			protected Form<T> createForm() {
				return SimpleTablePageWithDetail.this.createForm(Form.EDITABLE);
			}
		}
		
		public class NewDetailEditor extends TablePage<T>.NewDetailEditor {

			@Override
			protected Form<T> createForm() {
				return SimpleTablePageWithDetail.this.createForm(Form.EDITABLE);
			}
		}
		
		public class DetailPage extends Page {

			private transient Form<T> form;
			private T detail;

			public DetailPage(T detail) {
		  		this.detail = detail;
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
				return Arrays.asList(new DetailEditor());
			}
			
			public IContent getContent() {
				if (form == null) {
					form = createForm(Form.READ_ONLY);
				}
				form.setObject(detail);
				return form.getContent();
			}
			
			public void setDetail(T detail) {
				this.detail = detail;
				if (form != null) {
					form.setObject(detail);
				}
			}
			
			public class DetailEditor extends SimpleEditor<T> {

				@Override
				protected Object[] getNameArguments() {
					return SimpleTablePageWithDetail.this.nameArguments;
				}
				
				@Override
				protected Form<T> createForm() {
					return SimpleTablePageWithDetail.this.createForm(Form.EDITABLE);
				}
				
				@Override
				protected T createObject() {
					return detail;
				}
				
				@Override
				protected T save(T object) {
					return Backend.save(object);
				}
				
				@Override
				protected void finished(T result) {
					SimpleTablePageWithDetail.this.refresh();
					SimpleTablePageWithDetail.this.action(result);
				}
			}	
		}
		
		@Override
		protected DetailPage createDetailPage(T detail) {
			return new DetailPage(detail);
		}
		
		@Override
		protected DetailPage updateDetailPage(SimpleTablePageWithDetail<T>.DetailPage detailPage, T mainObject) {
			detailPage.setDetail(mainObject);
			return detailPage;
		}
	}
	
}