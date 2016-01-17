package org.minimalj.frontend.form.element;

import java.util.List;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.resources.Resources;

public abstract class ListFormElement<T> extends AbstractObjectFormElement<List<T>> {
	// private static final Logger logger = Logger.getLogger(ObjectField.class.getName());
	
	public ListFormElement(PropertyInterface property) {
		this(property, true);
	}

	public ListFormElement(PropertyInterface property, boolean editable) {
		super(property, editable);
	}
	
	@Override
	protected void show(List<T> objects) {
		for (T entry : objects) {
			showEntry(entry);
		}
	}

	protected abstract void showEntry(T entry);
	
	protected abstract Form<T> createForm(boolean edit);
	
	private abstract class ListFormElementEditor extends Editor<T, Void> {
		public ListFormElementEditor() {
			assertEditable(this);
		}

		public ListFormElementEditor(String name) {
			super(name);
			assertEditable(this);
		}
		
		@Override
		protected Object[] getNameArguments() {
			Class<?> editedClass = GenericUtils.getGenericClass(ListFormElement.this.getClass());
			if (editedClass != null) {
				String resourceName = Resources.getResourceName(editedClass);
				return new Object[] { Resources.getString(resourceName) };
			} else {
				return null;
			}
		}
	}
	
	public class AddListEntryEditor extends ListFormElementEditor {
		
		public AddListEntryEditor() {
		}

		public AddListEntryEditor(String name) {
			super(name);
		}
		
		@Override
		protected T createObject() {
			@SuppressWarnings("unchecked")
			Class<T> clazz = (Class<T>) org.minimalj.util.GenericUtils.getGenericClass(ListFormElement.this.getClass());
			T newInstance = CloneHelper.newInstance(clazz);
			return newInstance;
		}
		
		@Override
		protected Form<T> createForm() {
			return ListFormElement.this.createForm(false);
		}
		
		@Override
		public Void save(T entry) {
			addEntry(entry);
			return null;
		}

		protected void addEntry(T entry) {
			getValue().add(entry);
		}

		@Override
		protected void finished(Void result) {
			handleChange();
		}
	}

	protected Action getEditorAction() {
		throw new RuntimeException(getClass().getSimpleName() + " must not use getEditorAction. Please use an extension of EditListEntryAction");
	}

	public class ListEntryEditor extends ListFormElementEditor {
		private final T originalEntry;
		
		public ListEntryEditor(T originalEntry) {
			this.originalEntry = originalEntry;
		}
		
		@Override
		protected T createObject() {
			return CloneHelper.clone(originalEntry);
		}
		
		@Override
		protected Form<T> createForm() {
			return ListFormElement.this.createForm(true);
		}

		@Override
		public Void save(T entry) {
			editEntry(originalEntry, entry);
			return null;
		}

		protected void editEntry(T originalEntry, T entry) {
			CloneHelper.deepCopy(entry, originalEntry);
		}

		@Override
		protected void finished(Void result) {
			handleChange();
		}
	}

}
