package ch.openech.mj.edit.fields;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import ch.openech.mj.edit.Editor;
import ch.openech.mj.edit.EditorDialogAction;

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
	
	public ObjectFlowField(Object key) {
		this(key, true);
	}

	public ObjectFlowField(Object key, boolean editable) {
		super(key, editable);
		
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

		protected abstract P getPart(T object);

		protected abstract void setPart(T object, P p);
		
	}
	
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
	
}
