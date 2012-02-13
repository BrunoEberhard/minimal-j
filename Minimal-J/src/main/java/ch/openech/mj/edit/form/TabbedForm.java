package ch.openech.mj.edit.form;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.edit.value.PropertyAccessor;

public class TabbedForm<T> extends JTabbedPane implements FormVisual<T> {

	private final LinkedHashMap<String, FormVisual<?>> forms = new LinkedHashMap<String, FormVisual<?>>();
	private final JTabbedPane tabbedPane;
	private final ChangeListener tabbedFormchangeListener = new TabbedFormChangeListener();
	private ChangeListener changeListener;
	private T object;
	
	public TabbedForm() {
		setLayout(new BorderLayout());
		tabbedPane = new JTabbedPane();
		add(tabbedPane, BorderLayout.CENTER);
	}
	
	public void addForm(String property, FormVisual<?> form) {
		if (property == null && !forms.isEmpty()) {
			throw new IllegalArgumentException("Only first tab may edit object itself");
		}
		forms.put(property, form);
		tabbedPane.add((Component) form.getComponent());
		form.setChangeListener(tabbedFormchangeListener);
	}
	
	
	@Override
	public void validate(List<ValidationMessage> resultList) {
		for (FormVisual<?> form : forms.values()) {
			form.validate(resultList);
		}
	}

	@Override
	public void setChangeListener(ChangeListener changeListener) {
		this.changeListener = changeListener;
	}

	@Override
	public void setSaveAction(Action saveAction) {
		for (FormVisual<?> form : forms.values()) {
			form.setSaveAction(saveAction);
		}
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		for (FormVisual<?> form : forms.values()) {
			form.setValidationMessages(validationMessages);
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setObject(T object) {
		this.object = object;
		for (Map.Entry<String, FormVisual<?>> entry : forms.entrySet()) {
			Object value = PropertyAccessor.get(object, entry.getKey());
			FormVisual form = (FormVisual) entry.getValue();
			form.setObject(value);
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public T getObject() {
		T result = object;
		for (Map.Entry<String, FormVisual<?>> entry : forms.entrySet()) {
			FormVisual form = (FormVisual) entry.getValue();
			Object value = form.getObject();
			if (entry.getKey() == null) {
				result = (T) value;
			} else if (PropertyAccessor.get(result, entry.getKey()) != value) {
				PropertyAccessor.set(result, entry.getKey(), value);
			}
		}
		return result;
	}

	@Override
	public boolean isResizable() {
		for (FormVisual<?> form : forms.values()) {
			if (form.isResizable()) return true;
		}
		return false;
	}

	private class TabbedFormChangeListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			if (TabbedForm.this.changeListener != null) {
				TabbedForm.this.changeListener.stateChanged(e);
			}
		}
	}

	@Override
	public Object getComponent() {
		return tabbedPane;
	}
	
}
