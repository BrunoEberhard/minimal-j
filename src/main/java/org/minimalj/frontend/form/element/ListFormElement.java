package org.minimalj.frontend.form.element;

import java.util.List;

import org.minimalj.frontend.editor.Editor;
import org.minimalj.model.properties.PropertyInterface;

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

	public abstract class AddListEntryAction extends Editor<T, Void> {
		
		public AddListEntryAction() {
		}

		public AddListEntryAction(String name) {
			super(name);
		}
		
		@Override
		public Void save(T entry) {
			addEntry(entry);
			return null;
		}

		protected abstract void addEntry(T entry);

		@Override
		protected void finished(Void result) {
			handleChange();
		}
	}

	public abstract class EditListEntryAction extends Editor<T, Void> {
		private final T originalEntry;
		
		public EditListEntryAction(T originalEntry) {
			this.originalEntry = originalEntry;
		}

		@Override
		public Void save(T entry) {
			editEntry(originalEntry, entry);
			return null;
		}

		protected abstract void editEntry(T originalEntry, T entry);

		@Override
		protected void finished(Void result) {
			handleChange();
		}
	}
	
}
