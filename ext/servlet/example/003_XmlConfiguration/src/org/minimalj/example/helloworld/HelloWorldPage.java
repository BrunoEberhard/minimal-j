package org.minimalj.example.helloworld;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.util.logging.Logger;

import javax.swing.Timer;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.Page;
import org.minimalj.model.Keys;

public class HelloWorldPage implements Page {
	private static final Logger logger = Logger.getLogger(HelloWorldPage.class.getName());

	private final Form<HelloModel> form = new Form<>(false);

	public HelloWorldPage() {
		new Timer(1000, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				logger.info("push update");
				HelloModel a = new HelloModel();
				a.text = "Hello. It's " + (LocalTime.now());
				form.setObject(a);
			}
		}).start();
	}

	@Override
	public String getTitle() {
		return "Hello World";
	}

	@Override
	public IContent getContent() {
		form.line(Form.readonly(HelloModel.$.text));
		return form.getContent();
	}

	public static class HelloModel {
		public static final HelloModel $ = Keys.of(HelloModel.class);

		public String text;
	}
}
