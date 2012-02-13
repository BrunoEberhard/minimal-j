package ch.openech.mj.vaadin.widgetset.client.ui;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Command;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ui.VTextField;

public class VVaadinTextField  extends VTextField {

	public static final String TEXT_RESPONSE = "textResponse";
	public static final String TEXT_REQUEST = "textRequest";

	private String uidlId;
	private ApplicationConnection client;
	
	public VVaadinTextField() {
		addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				Scheduler.get().scheduleDeferred(new Command() {
					@Override
					public void execute() {
						String enteredText = getText();
						client.updateVariable(uidlId, TEXT_REQUEST, enteredText, true);
					}
				});
			}
		});
	}

 	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		super.updateFromUIDL(uidl, client);
		this.client = client;
		this.uidlId = uidl.getId();
		
		if (uidl.hasAttribute(TEXT_RESPONSE)) {
			setText(uidl.getStringAttribute(TEXT_RESPONSE));
		}
	}
	
}

