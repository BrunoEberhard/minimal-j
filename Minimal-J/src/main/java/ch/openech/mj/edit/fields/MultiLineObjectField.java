package ch.openech.mj.edit.fields;

import javax.swing.Action;

import ch.openech.mj.edit.Editor;
import ch.openech.mj.edit.EditorDialogAction;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.MultiLineTextField;

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
public abstract class MultiLineObjectField<T> extends ObjectField<T> implements Indicator {
	// private static final Logger logger = Logger.getLogger(ObjectField.class.getName());
	
	private final MultiLineTextField visual;
	
	public MultiLineObjectField(Object key) {
		this(key, true);
	}
	
	public MultiLineObjectField(Object key, boolean editable) {
		super(key, editable);
		visual = ClientToolkit.getToolkit().createMultiLineTextField();
	}

	protected void setText(Object object) {
		clearVisual();
		addObject(object);
	}
	
	protected void addObject(Object object) {
		visual.addObject(object);
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
	
	protected void clearVisual() {
		visual.clear();
	}

	protected MultiLineTextField getVisual() {
		return visual;
	}

	@Override
	public Object getComponent() {
		return visual;
	}
	
}
