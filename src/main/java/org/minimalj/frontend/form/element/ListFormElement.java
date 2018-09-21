package org.minimalj.frontend.form.element;

import java.util.List;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.GenericUtils;

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

	protected void showEntry(T entry) {
		if (isEditable()) {
			add(entry, new ListEntryEditor(entry), new RemoveEntryAction(entry));
		} else {
			add(entry);
		}
	}

	@Override
	protected Action[] getActions() {
		return new Action[] { new AddListEntryEditor() };
	}
	
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
		protected Class<T> getEditedClass() {
			return ListFormElement.this.getEditedClass();
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Class<T> getEditedClass() {
		return (Class<T>) GenericUtils.getGenericClass(getClass());
	}

	protected T createEntry() {
		return CloneHelper.newInstance(getEditedClass());
	}
	
	public class AddListEntryEditor extends ListFormElementEditor {
		
		public AddListEntryEditor() {
		}

		public AddListEntryEditor(String name) {
			super(name);
		}
		
		@Override
		protected T createObject() {
			return ListFormElement.this.createEntry();
		}
		
		@Override
		protected Form<T> createForm() {
			return ListFormElement.this.createForm(true);
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
	
	protected class RemoveEntryAction extends Action {
		private final T entry;
		
		public RemoveEntryAction(T entry) {
			this.entry = entry;
		}
		
		@Override
		public void action() {
			getValue().remove(entry);
			handleChange();
		}
    };

}
