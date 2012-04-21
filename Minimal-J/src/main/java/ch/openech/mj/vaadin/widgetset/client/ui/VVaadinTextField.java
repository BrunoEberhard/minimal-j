package ch.openech.mj.vaadin.widgetset.client.ui;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Command;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ui.VTextField;

public class VVaadinTextField  extends VTextField {

	public static final String ALLOWED_CHARACTERS = "allowedCharacters";
	public static final String LIMIT = "limit";

	private String allowedCharacters;
	private int limit;
	
	public VVaadinTextField() {
		addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				Scheduler.get().scheduleDeferred(new Command() {
					@Override
					public void execute() {
						String text = getText();
						if (text != null) {
							text = filter(text);
							if (text.length() > limit) {
								text = text.substring(0, limit);
							}
							setText(text);
						}
					}
				});
			}
		});
	}

 	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		super.updateFromUIDL(uidl, client);
		this.client = client;
		
		if (uidl.hasAttribute(ALLOWED_CHARACTERS)) {
			allowedCharacters = uidl.getStringAttribute(ALLOWED_CHARACTERS);
		}
		if (uidl.hasAttribute(LIMIT)) {
			limit = uidl.getIntAttribute(LIMIT);
		}
	}
	
	private String filter(String s) {
		String result = "";
		for (int i = 0; i<s.length(); i++) {
			char c = s.charAt(i);
			if (allowedCharacters != null) {
				if (allowedCharacters.indexOf(c) < 0) {
					continue;
				}
			}
			result += c;
		}
		return result;
	}
	
}

