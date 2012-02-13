package ch.openech.mj.edit.form;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.List;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.edit.value.PropertyAccessor;

public class FormAdapter<T> extends JPanel implements FormVisual<T> {

	private final Component component;
	private T object;
	
	public FormAdapter(Component component) {
		this.component = component;
		setLayout(new BorderLayout());
		add(component, BorderLayout.CENTER);
	}

	@Override
	public Object getComponent() {
		return component;
	}

	@Override
	public void validate(List<ValidationMessage> resultList) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setChangeListener(ChangeListener changeListener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setSaveAction(Action saveAction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setObject(T object) {
		this.object = object;
		writesValueToFields(this);
	}

	private void writesValueToFields(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof JCheckBox) {
				JCheckBox checkBox = (JCheckBox) component;
				String name = component.getName();
				Object value = PropertyAccessor.get(object, name);
				checkBox.setSelected(Boolean.TRUE.equals(value));
			} else if (component instanceof JTextField) {	
				// TODO
			} else if (component instanceof Container) {
				writesValueToFields((Container) component);
			}
		}
	}
	
	@Override
	public T getObject() {
		readValueFromFields(this);
		return object;
	}
	
	private void readValueFromFields(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof JCheckBox) {
				JCheckBox checkBox = (JCheckBox) component;
				String name = component.getName();
				Boolean selected = Boolean.valueOf(checkBox.isSelected());
				PropertyAccessor.set(object, name, selected);
			} else if (component instanceof JTextField) {	
				// TODO
			} else if (component instanceof Container) {
				readValueFromFields((Container) component);
			}
		}
	}

	@Override
	public boolean isResizable() {
		return component.getPreferredSize().equals(component.getMaximumSize());
	}
	
	
}
