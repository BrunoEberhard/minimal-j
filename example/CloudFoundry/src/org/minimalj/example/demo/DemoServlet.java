package org.minimalj.example.demo;

import java.util.Locale;

import org.minimalj.application.Application;
import org.minimalj.example.empty.EmptyApplication;
import org.minimalj.example.helloworld.HelloWorldApplication;
import org.minimalj.example.helloworld2.GreetingApplication;
import org.minimalj.example.library.MjExampleApplication;
import org.minimalj.example.notes.NotesApplication;
import org.minimalj.example.numbers.NumbersApplication;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.servlet.MjServlet;

public class DemoServlet extends MjServlet {
	private static final long serialVersionUID = 1L;

	private static boolean applicationInitialized;
	
	@Override
	protected void initializeApplication() {
		if (!applicationInitialized) {
			Frontend.setInstance(new JsonFrontend());

			System.setProperty("MjBackend", MultiBackend.class.getName());
			
			MultiApplication application = new MultiApplication();
			application.addApplication("empty", new EmptyApplication());
			application.addApplication("notes", new NotesApplication());
			application.addApplication("helloWorld", new HelloWorldApplication());
			application.addApplication("greeting", new GreetingApplication());
			application.addApplication("numbers", new NumbersApplication());
			application.addApplication("library", new MjExampleApplication());
			
			Application.setApplication(application);
			applicationInitialized = true;
		}
	}
	
	@Override
	protected String fillPlaceHolder(String htmlTemplate, Locale locale, String url) {
		String result = htmlTemplate.replace("$LOCALE", locale.toLanguageTag());
		if (url.indexOf("pivotal") > -1 || url.indexOf("cfapps.io") > -1) {
			// http://support.run.pivotal.io/entries/80621715-Does-cloudfoundry-allows-the-websocket-requests-on-port-other-than-4443-
			result = result.replace("$FORCE_WSS", "true");
			result = result.replace("$PORT", ":4443");
		} else {
			result = result.replace("$FORCE_WSS", "false");
			result = result.replace("$PORT", "");
		}
		result = result.replace("$WS", "wsDemo");
		return result;
	}
}