package ch.openech.mj.vaadin.toolkit;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

import ch.openech.mj.util.StringUtils;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class VaadinEditorLayout extends VerticalLayout {

	public VaadinEditorLayout(String information, ComponentContainer content, Action[] actions) {
		addInformation(information);
		
		content.setWidth("100%");
		GridLayout formAlign = (GridLayout) VaadinClientToolkit.getToolkit().createFormAlignLayout(content);
		formAlign.setWidth("100%");
		
		Panel panel = new Panel();
		panel.setContent(formAlign);
		panel.setScrollable(true);
		
		panel.setSizeFull();
		addComponent(panel);
		setExpandRatio(panel, 1.0F);
		
		addComponent(createButtonBar(actions));
	}

	protected void addInformation(String information) {
		if (!StringUtils.isBlank(information)) {
			Label help = new Label(information);
			addComponent(help);
		}
	}

	protected Component createButtonBar(Action... actions) {
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.addStyleName("buttonBar");
		horizontalLayout.setWidth("100%");
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(true);
		
		addButtons(horizontalLayout, actions);
		return horizontalLayout;
	}
	
	protected void addButtons(HorizontalLayout buttonBar, Action... actions) {
		for (Action action: actions) {
			addActionButton(buttonBar, action);
		}
		if (buttonBar.getComponentCount() > 0) {
			buttonBar.setExpandRatio(buttonBar.getComponent(0), 1.0F);
		}
	}

	protected void addActionButton(HorizontalLayout buttonBar, final Action action) {
		Button button = new NativeButton((String) action.getValue(Action.NAME));
		button.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				action.actionPerformed(null);
			}
		});
		installAdditionalActionListener(action, button);
		buttonBar.addComponent(button);
		buttonBar.setComponentAlignment(button, Alignment.MIDDLE_RIGHT);
	}
	
	private static void installAdditionalActionListener(Action action, final Button button) {
		action.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("visible".equals(evt.getPropertyName()) && (evt.getNewValue() instanceof Boolean)) {
					button.setVisible((Boolean) evt.getNewValue());
				} else if ("foreground".equals(evt.getPropertyName()) && (evt.getNewValue() instanceof Color)) {
					// TODO Color of Vaadion ButtonBar Buttons
					// button.set((Color) evt.getNewValue());
				}
			}
		});
	}
}
