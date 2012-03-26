package ch.openech.mj.vaadin.toolkit;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;

import ch.openech.mj.toolkit.ContextLayout;
import ch.openech.mj.toolkit.IComponent;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.VerticalLayout;

public class VaadinContextLayout extends HorizontalLayout implements ContextLayout {

	private Button firstButton;
	private PopupView pv;
	
	public VaadinContextLayout(IComponent content) {
		Component component = VaadinClientToolkit.getComponent(content);
		component.setWidth("100%");
		
		addComponent(component);
		this.setExpandRatio(component, 1.0F);
	}

	@Override
	public void setActions(List<Action> actions) {
		setActions(actions.toArray(new Action[actions.size()]));
	}

	@Override
	public void setActions(Action... actions) {
		if (actions.length == 0) return;
		addComponent(createContextMenu(actions));
	}

    private Component createContextMenu(Action[] actions) {
        final VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidth("200px");
        
        for (Action action : actions) {
        	Button button = createButton(action);
        	verticalLayout.addComponent(button);
        }
        
        pv = new PopupView("<span></span>", verticalLayout);
        pv.addListener(new PopupView.PopupVisibilityListener() {
            @Override
			public void popupVisibilityChange(PopupVisibilityEvent event) {
                if (event.isPopupVisible()) {
                	firstButton.focus();
                }
            }
        });
        pv.setStyleName("contextMenu");
        pv.setDescription("Kontext Menu");
        pv.setWidth("24px");
        pv.setHeight("24px");
        
        return pv;
    }
    
    private Button createButton(final Action action) {
    	final Button button = new NativeButton();
    	button.setStyleName("borderless");
    	button.setCaption((String) action.getValue(Action.NAME));
    	button.setDescription((String) action.getValue(Action.LONG_DESCRIPTION));
    	if (firstButton == null) {
    		firstButton = button;
    	}
    	button.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				action.actionPerformed(new ActionEvent(button, 0, null));
				pv.setPopupVisible(false);
			}
		});
    	return button;
    }
}
