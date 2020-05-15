package org.minimalj.frontend.impl.vaadin.toolkit;

import org.apache.commons.lang3.StringUtils;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.action.Action;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;


public class VaadinEditorLayout extends VerticalLayout implements IComponent {
	private static final long serialVersionUID = 1L;

	public VaadinEditorLayout(String title, Component component, Action saveAction, Action closeAction) {
		setMargin(false);
		setSpacing(true);
		setPadding(false);
		setSizeFull();
		
		add(new Label(title));
		((HasSize) component).setSizeFull();
		add(component);	
		Component buttonBar = createButtonBar(saveAction, closeAction);
		add(buttonBar);
	}

	private Component createButtonBar(Action... actions) {
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		setSpacing(true);
		setWidthFull();
		horizontalLayout.addClassName("buttonBar");
		
		addButtons(horizontalLayout, actions);
		return horizontalLayout;
	}
	
	private void addButtons(HorizontalLayout buttonBar, Action... actions) {
		for (Action action: actions) {
			addActionButton(buttonBar, action);
		}
//		if (buttonBar.getComponentCount() > 0) {
//			buttonBar.setExpandRatio(buttonBar.getComponentAt(0), 1.0F);
//		}
	}

	private void addActionButton(HorizontalLayout buttonBar, final Action action) {
		final Button button = new Button(action.getName());
		button.setEnabled(action.isEnabled());
		if (!StringUtils.isEmpty(action.getDescription())) {
			button.getElement().setAttribute("title", action.getDescription());
		}
		button.setWidth((action.getName().length() + 5) + "ex");
		// installShortcut(button, action);
		button.addClickListener(event -> action.action());
		installActionListener(action, button);
		buttonBar.add(button);
		buttonBar.setAlignItems(Alignment.END);
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
				button.setText(action.getName());
				if (!StringUtils.isEmpty(action.getDescription())) {
					button.getElement().setAttribute("title", action.getDescription());
				} else {
					button.getElement().removeAttribute("title");
				}
			}
		});
	}
}
