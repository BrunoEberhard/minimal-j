package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.vaadin.VaadinBorderLayout;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;

public class VaadinEditorLayout extends VaadinBorderLayout implements IComponent {
	private static final long serialVersionUID = 1L;

	public VaadinEditorLayout(IContent content, Action[] actions) {
		setSizeFull();
		
		Component contentComponent = (Component) content;
		Panel scrollPanel = decorateWithScrollPanel((ComponentContainer) contentComponent);
		addComponent(scrollPanel, Constraint.CENTER);
		
		Component buttonBar = createButtonBar(actions);
		setMinimumSouthHeight("5ex");
		addComponent(buttonBar, Constraint.SOUTH);
	}

	private static Panel decorateWithScrollPanel(ComponentContainer content) {
		Panel scrollablePanel = new Panel(content);
		// scrollablePanel.setScrollable(true);
		scrollablePanel.setHeight("100%");
		return scrollablePanel;
	}
	
	private Component createButtonBar(Action... actions) {
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.addStyleName("buttonBar");
		horizontalLayout.setWidth("100%");
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(true);
		
		addButtons(horizontalLayout, actions);
		return horizontalLayout;
	}
	
	private void addButtons(HorizontalLayout buttonBar, Action... actions) {
		for (Action action: actions) {
			addActionButton(buttonBar, action);
		}
		if (buttonBar.getComponentCount() > 0) {
			buttonBar.setExpandRatio(buttonBar.getComponent(0), 1.0F);
		}
	}

	private void addActionButton(HorizontalLayout buttonBar, final Action action) {
		final Button button = new NativeButton(action.getName());
		button.setEnabled(action.isEnabled());
		button.setDescription(action.getDescription());
		button.setWidth((action.getName().length() + 5) + "ex");
		// installShortcut(button, action);
		button.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				action.action();
			}
		});
		installActionListener(action, button);
		buttonBar.addComponent(button);
		buttonBar.setComponentAlignment(button, Alignment.MIDDLE_RIGHT);
	}
	
//	private static void installShortcut(Button button, Action action) {
//		KeyStroke key = (KeyStroke)action.getValue(Action.ACCELERATOR_KEY);
//		if (key != null) {
//			button.setClickShortcut(key.getKeyCode(), convertSwingModifierToVaadin(key.getModifiers()));
//		}
// 	}
//	
//	private static int[] convertSwingModifierToVaadin(int modifier) {
//		List<Integer> modifiers = new ArrayList<>(4);
//		if ((modifier & InputEvent.SHIFT_DOWN_MASK) > 0) {
//			modifiers.add(ShortcutAction.ModifierKey.SHIFT);
//		}
//		if ((modifier & InputEvent.CTRL_DOWN_MASK) > 0) {
//			modifiers.add(ShortcutAction.ModifierKey.CTRL);
//		}
//		if ((modifier & InputEvent.ALT_DOWN_MASK) > 0) {
//			modifiers.add(ShortcutAction.ModifierKey.ALT);
//		}
//		if ((modifier & InputEvent.META_DOWN_MASK) > 0) {
//			modifiers.add(ShortcutAction.ModifierKey.META);
//		}
//		int[] result = new int[modifiers.size()];
//		for (int i = 0; i<result.length; i++) {
//			result[i] = modifiers.get(i);
//		}
//		return result;
//	}
	
	private static void installActionListener(final Action action, final Button button) {
		action.setChangeListener(new Action.ActionChangeListener() {
			
			@Override
			public void change() {
				button.setEnabled(action.isEnabled());
				button.setCaption(action.getName());
				button.setDescription(action.getDescription());
			}
		});
	}
}
