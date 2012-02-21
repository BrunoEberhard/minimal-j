package ch.openech.mj.edit.fields;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.db.model.Constants;
import ch.openech.mj.edit.ChangeableValue;
import ch.openech.mj.edit.Editor;
import ch.openech.mj.edit.EditorDialogAction;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ContextLayout;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.IComponentDelegate;
import ch.openech.mj.toolkit.SwitchLayout;
import ch.openech.mj.util.StringUtils;

public abstract class AbstractEditField<T> implements IComponentDelegate, EditField<T>, Indicator {

	private final String name;
	
	private ChangeListener forwardingChangeListener;
	private ChangeListener changeListener;
	
	private final List<Action> actions = new ArrayList<Action>();
	private ContextLayout contextLayout;
	
	protected AbstractEditField(Object key) {
		this.name = Constants.getConstant(key);
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public final Object getComponent() {
		IComponent component = getComponent0();
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
	
	
	protected void addAction(Action action) {
		actions.add(action);
	}
	
	protected void addAction(Editor<?> editor) {
		actions.add(new EditorDialogAction(editor));
	}
	
	protected Indicator[] getIndicatingComponents() {
		IComponent component = getComponent0();
		while (component instanceof SwitchLayout) {
			SwitchLayout switchLayout = (SwitchLayout) component;
			component = switchLayout.getShownComponent();
		}
		if (component instanceof Indicator) {
			return new Indicator[]{(Indicator) component};
		} else {
			// TODO warn
			return new Indicator[0];
		}
	}
	
	@Override
	public final void setValidationMessages(List<ValidationMessage> validationMessages) {
		for (Indicator indicator : getIndicatingComponents()) {
			indicator.setValidationMessages(validationMessages);
		}
	}
	
	protected abstract IComponent getComponent0();
	//
	
	@Override
	public boolean isEmpty() {
		Object object = getObject();
		return object == null || (object instanceof String) && StringUtils.isEmpty((String) object);
	}
	
	// Listener
	
	protected void listenTo(ChangeableValue<?> changeable) {
		changeable.setChangeListener(listener());
	}

	protected ChangeListener listener() {
		if (forwardingChangeListener == null) {
			forwardingChangeListener = new ForwardingChangeListener();
		}
		return forwardingChangeListener;
	}
	
	@Override
	public void setChangeListener(ChangeListener changeListener) {
		this.changeListener = changeListener;
	}

	protected void fireChange() {
		if (changeListener != null) {
			changeListener.stateChanged(new ChangeEvent(AbstractEditField.this));
		}
	}
	
	private class ForwardingChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			fireChange();
		}
	}
	
	//
	
	protected static void showBubble(IComponent component, String text) {
		ClientToolkit.getToolkit().showNotification(component, text);
	}
	
}
