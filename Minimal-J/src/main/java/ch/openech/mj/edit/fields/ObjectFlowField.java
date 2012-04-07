package ch.openech.mj.edit.fields;

import javax.swing.Action;

import ch.openech.mj.edit.Editor;
import ch.openech.mj.edit.EditorDialogAction;
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
