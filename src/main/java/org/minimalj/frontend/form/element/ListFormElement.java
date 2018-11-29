package org.minimalj.frontend.form.element;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IList;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.GenericUtils;

public abstract class ListFormElement<T> extends AbstractFormElement<List<T>> {
	private static final Logger logger = Logger.getLogger(ListFormElement.class.getName());
	
	private final boolean editable;
	private final IList list;
	private List<T> object;

	public ListFormElement(PropertyInterface property) {
		this(property, true);
	}

	public ListFormElement(PropertyInterface property, boolean editable) {
		super(property);
		this.editable = editable;
		list = editable ? Frontend.getInstance().createList(getActions()) : Frontend.getInstance().createList();
	}
	
	protected final boolean isEditable() {
		return editable;
	}

	@Override
	public List<T> getValue() {
		return object;
	}

	@Override
	public void setValue(List<T> object) {
		this.object = object;
		handleChange();
	}

	@Override
	public IComponent getComponent() {
		return list;
	}

	protected void handleChange() {
		display();
		super.fireChange();
	}

	protected void display() {
		list.clear();
		if (object != null) {
			show(object);
		}
	}

	protected void show(List<T> objects) {
		objects.forEach(this::showEntry);
	}

	protected void showEntry(T entry) {
		if (isEditable()) {
			add(entry, new ListEntryEditor(entry), new RemoveEntryAction(entry));
		} else {
			add(entry);
		}
	}

	protected void add(String text, Action... actions) {
		add((Object) text, actions);
	}

	protected void add(Object object, Action... actions) {
		list.add(object, actions);
	}

	protected void add(String title, Object object, Action... actions) {
		list.add(title, object, actions);
	}

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
	
	protected void assertEditable(Object object) {
		if (!isEditable()) {
			String msg = object.getClass().getSimpleName() + " should not be used if " + ListFormElement.class.getSimpleName() + " is not editable";
			if (Configuration.isDevModeActive()) {
				throw new IllegalArgumentException(msg);
			} else {
				logger.warning(msg);
			}
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

		@Override
		protected void finished(Void result) {
			handleChange();
		}
	}

	protected void addEntry(T entry) {
		if (object == null) {
			object = new ArrayList<>();
		}
		object.add(entry);
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

		@Override
		protected void finished(Void result) {
			handleChange();
		}
	}
	
	protected void editEntry(T originalEntry, T entry) {
		CloneHelper.deepCopy(entry, originalEntry);
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
