package ch.openech.mj.edit.fields;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import ch.openech.mj.edit.Editor;
import ch.openech.mj.edit.EditorDialogAction;
import ch.openech.mj.edit.form.FormVisual;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.FlowField;

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
public abstract class ObjectFlowField<T> extends ObjectField<T> {
	// private static final Logger logger = Logger.getLogger(ObjectField.class.getName());
	
	private final FlowField visual;
	
	public ObjectFlowField(Object key) {
		this(key, true);
	}

	public ObjectFlowField(Object key, boolean editable) {
		this(key, editable, true);
	}
	
	public ObjectFlowField(Object key, boolean editable, boolean vertical) {
		super(key, editable);
		visual = ClientToolkit.getToolkit().createFlowField(vertical);
	}
	

	public class ObjectFieldEditor extends Editor<T> {

		@Override
		public FormVisual<T> createForm() {
			return ObjectFlowField.this.createFormPanel();
		}

		@Override
		public T load() {
			if (getObject() != null) {
				return getObject();
			} else {
				Class<?> clazz = ch.openech.mj.util.GenericUtils.getGenericClass(ObjectFlowField.this.getClass());
				if (clazz == null) {
					throw new RuntimeException("TODO");
				}
				try {
					return (T) clazz.newInstance();
				} catch (InstantiationException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}

		@Override
		public boolean save(T object) {
			setObject(object);
			return true;
		}

		@Override
		public void validate(T object, List<ValidationMessage> resultList) {
			// may be overwritten
		}
	}

	public abstract class ObjectFieldPartEditor<P> extends Editor<P> {

		@Override
		public P load() {
			return getPart(ObjectFlowField.this.getObject());
		}

		@Override
		public boolean save(P part) {
			setPart(ObjectFlowField.this.getObject(), part);
			fireObjectChange();
			return true;
		}

		@Override
		public void validate(P object, List<ValidationMessage> resultList) {
			// may be overwritten
		}

		protected abstract P getPart(T object);

		protected abstract void setPart(T object, P p);
		
	}

	protected abstract FormVisual<T> createFormPanel();
	
	// why public
	public class RemoveObjectAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			ObjectFlowField.this.setObject(null);
		}
	}
	
	protected void addObject(Object object) {
		visual.addObject(object);
	}

	protected void addHtml(String html) {
		visual.addHtml(html);
	}

	protected void addGap() {
		visual.addGap();
	}
	
	protected void addAction(Action action) {
		visual.addAction(action);
	}
	
	protected void addAction(Editor<?> editor) {
		visual.addAction(new EditorDialogAction(editor));
	}
	
	protected void addAction(Editor<?> editor, String actionName) {
		visual.addAction(new EditorDialogAction(editor, actionName));
	}
	
	@Override
	protected void fireObjectChange() {
		visual.clear();
		super.fireObjectChange();
	}
	
	protected FlowField getVisual() {
		return visual;
	}

	@Override
	public Object getComponent() {
		return visual;
	}
	
}
