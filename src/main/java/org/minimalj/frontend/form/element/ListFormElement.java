package org.minimalj.frontend.form.element;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.Rendering;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.GenericUtils;

public abstract class ListFormElement<T> extends AbstractFormElement<List<T>> {
	private final Input<List<T>> list; // only used when editable
	private final Input<String> text; // only used when read only
	private List<T> object;

	public ListFormElement(PropertyInterface property) {
		this(property, Form.EDITABLE);
	}

	public ListFormElement(PropertyInterface property, boolean editable) {
		super(property);
		list = editable ? Frontend.getInstance().createList(this::render, this::getActions, getActions()) : null;
		text = editable ? null : Frontend.getInstance().createReadOnlyTextField();
		height(1, 3);
	}
	
	protected final boolean isEditable() {
		return list != null;
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
		return list != null ? list : text;
	}

	protected void handleChange() {
		display();
		super.fireChange();
	}

	private void display() {
		if (list != null) {
			list.setValue(object);
		} else {
			text.setValue("");
			if (object != null) {
				for (T item : object) {
					String newValue = text.getValue() + "\n" + render(item);
					text.setValue(newValue.trim());
				}
			}
		}
	}

	protected CharSequence render(T item) {
		return Rendering.render(item);
	}

//	protected Object render(T value) {
//		if (value instanceof Rendering) {
//			return value;
//		} else {
//			return Rendering.toString(value);
//		}
//	}

	protected Action[] getActions() {
		return new Action[] { new AddListEntryEditor() };
	}
	
	protected List<Action> getActions(T entry) {
		List<Action> list = new ArrayList<>();
		list.add(new ListEntryEditor(entry));
		list.add(new RemoveEntryAction(entry));
		return list;
	}

	protected abstract Form<T> createForm(boolean edit);
	
	private abstract class ListFormElementEditor extends Editor<T, Void> {
		public ListFormElementEditor() {
		}

		public ListFormElementEditor(String name) {
			super(name);
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
