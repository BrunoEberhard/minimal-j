package org.minimalj.frontend.edit.fields;

import org.minimalj.frontend.edit.Editor;
import org.minimalj.frontend.edit.EditorAction;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IContext;
import org.minimalj.frontend.toolkit.IAction;
import org.minimalj.frontend.toolkit.ResourceAction;
import org.minimalj.model.PropertyInterface;

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
	
	public ObjectFlowField(PropertyInterface property) {
		this(property, true);
	}

	public ObjectFlowField(PropertyInterface property, boolean editable) {
		super(property, editable);
		
	}

	public abstract class ObjectFieldPartEditor<P> extends Editor<P> {
		private final String title;
		
		public ObjectFieldPartEditor() {
			this.title = null;
		}
		
		public ObjectFieldPartEditor(String title) {
			this.title = title;
		}

		@Override
		public final String getTitle() {
			if (title != null) {
				return title;
			} else {
				return super.getTitle();
			}
		}

		@Override
		public P load() {
			return getPart(ObjectFlowField.this.getObject());
		}
		
		@Override
		public Object save(P part) {
			setPart(ObjectFlowField.this.getObject(), part);
			fireObjectChange();
			return SAVE_SUCCESSFUL;
		}

		protected abstract P getPart(T object);

		protected abstract void setPart(T object, P p);
		
	}
	
	// why public
	public class RemoveObjectAction extends ResourceAction {
		@Override
		public void action(IContext context) {
			ObjectFlowField.this.setObject(null);
		}
	}
	
	protected void addObject(Object object) {
		if (object != null) {
			addText(object.toString());
		}
	}

	protected void addText(String htmlText) {
		if (htmlText != null) {
			flowField.add(ClientToolkit.getToolkit().createLabel(htmlText));
		}
	}
	
	protected void addGap() {
		flowField.addGap();
	}
	
	protected void addAction(IAction action) {
		flowField.add(ClientToolkit.getToolkit().createLabel(action));
	}

	protected void addLink(String text, String address) {
		flowField.add(ClientToolkit.getToolkit().createLink(text, address));
	}
	
	protected void addAction(Editor<?> editor) {
		addAction(new EditorAction(editor));
	}

	protected void addAction(Editor<?> editor, String actionName) {
		addAction(new EditorAction(editor, actionName));
	}

}
