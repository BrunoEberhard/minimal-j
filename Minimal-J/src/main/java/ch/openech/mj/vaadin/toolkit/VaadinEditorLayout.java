package ch.openech.mj.vaadin.toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import ch.openech.mj.toolkit.IAction;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;

public class VaadinEditorLayout extends VerticalLayout implements IComponent {
	private static final long serialVersionUID = 1L;

	public VaadinEditorLayout(IComponent content, IAction[] actions) {
		this(null, content, actions);
	}

	public VaadinEditorLayout(TextField text, Action searchAction, IComponent content, IAction[] actions) {
		this(createHeaderComponent(text, searchAction), content, actions);
	}

	private VaadinEditorLayout(Component header, IComponent content, IAction[] actions) {
		if (header != null) {
			addComponent(header);
		}
		
		Component contentComponent = (Component) content;
		addComponent(contentComponent);
		
		Component buttonBar = createButtonBar(actions);
		addComponent(buttonBar);
	}

	private static Component createHeaderComponent(TextField text, final Action searchAction) {
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setWidth("100%");
		
		Component textFieldComponent = (Component) text;
		textFieldComponent.setWidth("100%");
        horizontalLayout.addComponent(textFieldComponent);
        horizontalLayout.setExpandRatio(textFieldComponent, 1.0F);
        
        final Button button = new Button("Suche");
        button.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				searchAction.actionPerformed(new ActionEvent(button, 0, null));
			}
		});
        
        AbstractField field = findAbstractField(textFieldComponent);
        if (field != null) {
        	field.addShortcutListener(new ShortcutListener("Search", ShortcutAction.KeyCode.ENTER, null) {
    			private static final long serialVersionUID = 1L;

				@Override
    			public void handleAction(Object sender, Object target) {
    				searchAction.actionPerformed(new ActionEvent(button, 0, null));
    			}
    		});
        }
        
        horizontalLayout.addComponent(button);
        horizontalLayout.setExpandRatio(button, 0.0F);
		return horizontalLayout;
	}

	private static AbstractField findAbstractField(Component c) {
		if (c instanceof AbstractField) {
			return ((AbstractField) c);
		} else if (c instanceof ComponentContainer) {
			ComponentContainer container = (ComponentContainer) c;
			Iterator<Component> components = container.getComponentIterator();
			while (components.hasNext()) {
				AbstractField field = findAbstractField(components.next());
				if (field != null) {
					return field;
				}
			}
		}
		return null;
	}
	
	private Component createButtonBar(IAction... actions) {
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.addStyleName("buttonBar");
		horizontalLayout.setWidth("100%");
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(true);
		
		addButtons(horizontalLayout, actions);
		return horizontalLayout;
	}
	
	private void addButtons(HorizontalLayout buttonBar, IAction... actions) {
		for (IAction action: actions) {
			addActionButton(buttonBar, action);
		}
		if (buttonBar.getComponentCount() > 0) {
			buttonBar.setExpandRatio(buttonBar.getComponent(0), 1.0F);
		}
	}

	private void addActionButton(HorizontalLayout buttonBar, final IAction action) {
		final Button button = new NativeButton(action.getName());
		button.setEnabled(action.isEnabled());
		button.setDescription(action.getDescription());
		// installShortcut(button, action);
		button.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				action.action(VaadinEditorLayout.this);
			}
		});
		installActionListener(action, button);
		buttonBar.addComponent(button);
		buttonBar.setComponentAlignment(button, Alignment.MIDDLE_RIGHT);
	}
	
	private static void installShortcut(Button button, Action action) {
		KeyStroke key = (KeyStroke)action.getValue(Action.ACCELERATOR_KEY);
		if (key != null) {
			button.setClickShortcut(key.getKeyCode(), convertSwingModifierToVaadin(key.getModifiers()));
		}
 	}
	
	private static int[] convertSwingModifierToVaadin(int modifier) {
		List<Integer> modifiers = new ArrayList<>(4);
		if ((modifier & InputEvent.SHIFT_DOWN_MASK) > 0) {
			modifiers.add(ShortcutAction.ModifierKey.SHIFT);
		}
		if ((modifier & InputEvent.CTRL_DOWN_MASK) > 0) {
			modifiers.add(ShortcutAction.ModifierKey.CTRL);
		}
		if ((modifier & InputEvent.ALT_DOWN_MASK) > 0) {
			modifiers.add(ShortcutAction.ModifierKey.ALT);
		}
		if ((modifier & InputEvent.META_DOWN_MASK) > 0) {
			modifiers.add(ShortcutAction.ModifierKey.META);
		}
		int[] result = new int[modifiers.size()];
		for (int i = 0; i<result.length; i++) {
			result[i] = modifiers.get(i);
		}
		return result;
	}
	
	private static void installActionListener(final IAction action, final Button button) {
		action.setChangeListener(new IAction.ActionChangeListener() {
			
			@Override
			public void change() {
				button.setEnabled(action.isEnabled());
				button.setCaption(action.getName());
				button.setDescription(action.getDescription());
			}
		});
//		action.addPropertyChangeListener(new PropertyChangeListener() {
//			@Override
//			public void propertyChange(PropertyChangeEvent evt) {
//				if ("visible".equals(evt.getPropertyName()) && (evt.getNewValue() instanceof Boolean)) {
//					button.setVisible((Boolean) evt.getNewValue());
//				} else if ("foreground".equals(evt.getPropertyName()) && (evt.getNewValue() instanceof Color)) {
//					// TODO Color of Vaadion ButtonBar Buttons
//					// button.set((Color) evt.getNewValue());
//				} else if ("enabled".equals(evt.getPropertyName()) && (evt.getNewValue() instanceof Boolean)) {
//					button.setEnabled((Boolean) evt.getNewValue());
//				}
//			}
//		});
	}
}
