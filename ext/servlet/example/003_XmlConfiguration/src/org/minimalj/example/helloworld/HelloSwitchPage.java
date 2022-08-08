package org.minimalj.example.helloworld;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.util.logging.Logger;

import javax.swing.Timer;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.SwitchContent;
import org.minimalj.frontend.page.Page;

public class HelloSwitchPage implements Page {
	private static final Logger logger = Logger.getLogger(HelloWorldPage.class.getName());

	private SwitchContent content = Frontend.getInstance().createSwitchContent();

	public HelloSwitchPage() {
		new Timer(1000, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				logger.info("update");
				content.show(Frontend.getInstance().createHtmlContent("Hello. It's " + (LocalTime.now())));
			}
		}).start();
	}

	@Override
	public String getTitle() {
		return "Hello World";
	}

	@Override
	public IContent getContent() {
		return content;
	}

}
