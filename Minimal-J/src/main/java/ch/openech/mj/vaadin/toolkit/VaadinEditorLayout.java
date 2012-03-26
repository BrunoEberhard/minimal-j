package ch.openech.mj.vaadin.toolkit;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.util.StringUtils;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class VaadinEditorLayout extends VerticalLayout implements IComponent {

	public VaadinEditorLayout(String information, IComponent content, Action[] actions) {
		this(createHeaderComponent(information), content, actions);
	}

	public VaadinEditorLayout(TextField text, Action searchAction, IComponent content, Action[] actions) {
		this(createHeaderComponent(text, searchAction), content, actions);
	}

	private VaadinEditorLayout(Component header, IComponent content, Action[] actions) {
		if (header != null) {
			addComponent(header);
		}
		
		VaadinClientToolkit.getComponent(content).setWidth("100%");
		
		Panel panel = new Panel();
		panel.setContent((ComponentContainer) VaadinClientToolkit.getComponent(content));
		panel.setScrollable(true);
		
		panel.setSizeFull();
		addComponent(panel);
		setExpandRatio(panel, 1.0F);
		
		Component buttonBar = createButtonBar(actions);
		addComponent(buttonBar);
		setExpandRatio(buttonBar, 0.0F);
		setWidth("100%");
	}

	private static Component createHeaderComponent(String information) {
		if (!StringUtils.isBlank(information)) {
			return new Label(information);
		} else {
			return null;
		}
	}
	
	private static Component createHeaderComponent(TextField text, final Action searchAction) {
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setWidth("100%");
		
		Component textFieldComponent = VaadinClientToolkit.getComponent(text);
		textFieldComponent.setWidth("100%");
        horizontalLayout.addComponent(textFieldComponent);
        
        final Button button = new Button("Suche");
        button.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				searchAction.actionPerformed(new ActionEvent(button, 0, null));
			}
		});
        
        horizontalLayout.addComponent(button);
		return horizontalLayout;
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
		final Button button = new NativeButton((String) action.getValue(Action.NAME));
		button.setEnabled(Boolean.TRUE.equals(action.getValue("enabled")));
		button.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				action.actionPerformed(new ActionEvent(button, 0, null));
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
				} else if ("enabled".equals(evt.getPropertyName()) && (evt.getNewValue() instanceof Boolean)) {
					button.setEnabled((Boolean) evt.getNewValue());
				}
			}
		});
	}
}
