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
	private String allowedCharacters;
	
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
	}
	
	private String filter(String s) {
		String result = "";
		for (int i = 0; i<s.length(); i++) {
			char c = s.charAt(i);
			if (allowedCharacters != null && allowedCharacters.length() > 0) {
				if (allowedCharacters.indexOf(c) < 0) {
					if (allowedCharacters.indexOf(Character.toUpperCase(c)) < 0) {
						continue;
					} else {
						c = Character.toUpperCase(c);
					}
				} else  {
				}
			}
			result += c;
		}
		return result;
	}
	
}

