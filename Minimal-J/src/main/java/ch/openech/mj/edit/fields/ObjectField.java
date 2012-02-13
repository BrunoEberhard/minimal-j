package ch.openech.mj.edit.fields;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import ch.openech.mj.db.EmptyObjects;
import ch.openech.mj.edit.Editor;
import ch.openech.mj.edit.EditorDialogAction;
import ch.openech.mj.edit.form.FormVisual;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ContextLayout;

public abstract class ObjectField<T> extends AbstractEditField<T> implements Indicator {
	// private static final Logger logger = Logger.getLogger(ObjectField.class.getName());
	
	private T object;
	
	private final List<Action> actions = new ArrayList<Action>();
	private ContextLayout contextLayout;
	
	public ObjectField(Object key) {
		super(key);
	}
	
	protected void addAction(Action action) {
		actions.add(action);
	}
	
	protected void addAction(Editor<?> editor) {
		if (getComponent0() == null) {
			throw new IllegalStateException("You can use addAction only AFTER initialize the component returned at getComponent0()");
		}
		actions.add(new EditorDialogAction(getComponent0(), editor));
	}
	
	@Override
	public final Object getComponent() {
		Object component = getComponent0();
		if (!actions.isEmpty()) {
			if (contextLayout == null) {
				contextLayout = ClientToolkit.getToolkit().createContextLayout(component);
				contextLayout.setActions(actions);
			}
			return contextLayout;
		} else {
			return component;
		}
	}
	
	protected abstract Object getComponent0();
	
	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		// the getComponent() method wraps the component in a contextLayout which is not
		// an indicator. So we have to use getComponent0 here.
		if (getComponent0() instanceof Indicator) {
			Indicator indicator = (Indicator) getComponent0();
			indicator.setValidationMessages(validationMessages);
		} else {
			throw new RuntimeException("You must override setValidationMessages in " + this.getClass().getName());
		}
	}
	
	public class ObjectFieldEditor extends Editor<T> {

		@Override
		public FormVisual<T> createForm() {
			return ObjectField.this.createFormPanel();
		}

		@SuppressWarnings("unchecked")
		@Override
		public T load() {
			if (object != null) {
				return object;
			} else {
				Class<?> clazz = ch.openech.mj.util.GenericUtils.getGenericClass(ObjectField.this.getClass());
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
			return getPart(ObjectField.this.getObject());
		}

		@Override
		public boolean save(P part) {
			setPart(ObjectField.this.getObject(), part);
			display(ObjectField.this.getObject());
			fireChange();
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
			ObjectField.this.setObject(null);
		}
	}
	
	@Override
	public T getObject() {
		return object;
	}

	@Override
	public void setObject(T object) {
// Einige Felder machen das in ihrem setObject, anderem machen gar kein super.setObject
// Was ist schöner? Hier sich auf Generics verlassen, oder die ganzen new-Commands?
//		if (object == null) {
//			try {
//				object = (T) GenericUtils.getGenericClass(this.getClass()).newInstance();
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
		setAdjusting(true);
		this.object = object;
		display(object);
		setAdjusting(false);
		
		fireChange();
	}
	
	@Override
	public boolean isEmpty() {
		Object object = getObject();
		return object == null || EmptyObjects.isEmpty(object);
	}
	
	protected abstract void display(T object);



}
