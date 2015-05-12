package org.minimalj.frontend.form.element;

import java.util.List;

import org.minimalj.frontend.editor.Editor;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.CloneHelper;

/**
 * The state of an ObjectField is saved in the object variable.<p>
 * 
 * You have to implement for an ObjectField:
 * <ul>
 * <li>display: The widgets have to be updated according to the object</li>
 * <li>fireChange: The object has to be updated according the widgets</li>
 * </ul>
 *
 * @param <T>
 */
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

	public abstract class AddListEntryEditor extends Editor<T> {
		
		public AddListEntryEditor() {
		}

		@Override
		protected T newInstance() {
			@SuppressWarnings("unchecked")
			Class<T> clazz = (Class<T>) org.minimalj.util.GenericUtils.getGenericClass(ListFormElement.this.getClass());
			T newInstance = CloneHelper.newInstance(clazz);
			return newInstance;
		}
		
		@Override
		public Object save(T entry) {
			addEntry(entry);
			handleChange();
			return SAVE_SUCCESSFUL;
		}

		protected abstract void addEntry(T entry);
		
	}

	public abstract class EditListEntryEditor extends Editor<T> {
		private final T originalEntry;
		
		public EditListEntryEditor(T originalEntry) {
			this.originalEntry = originalEntry;
		}

		@Override
		public T load() {
			return originalEntry;
		}
		
		@Override
		public Object save(T entry) {
			editEntry(originalEntry, entry);
			handleChange();
			return SAVE_SUCCESSFUL;
		}

		protected abstract void editEntry(T originalEntry, T entry);
		
	}
	
}
