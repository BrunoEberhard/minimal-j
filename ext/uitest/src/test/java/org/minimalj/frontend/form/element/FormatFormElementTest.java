package org.minimalj.frontend.form.element;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.Test;
import org.minimalj.application.Application;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.impl.web.WebServer;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.test.PageContainerTestFacade;
import org.minimalj.test.PageContainerTestFacade.FormTestFacade;
import org.minimalj.test.html.HtmlTest;
import org.minimalj.test.html.HtmlTestFacade;

public class FormatFormElementTest extends HtmlTest {
	
	@Test
	public void test() {
		WebServer.start(new FormatFormElementTestApplication());

		HtmlTestFacade application = new HtmlTestFacade(getDriver());

		PageContainerTestFacade window = application.getCurrentPageContainerTestFacade();
		
		window.getNavigation().get(new FormatFormElementTestEditor().getName()).run();
		
		FormTestFacade form = window.getDialog().getForm();
		form.element(FormatFormElementTestEntity.$.date).setText("1.2.2023");
		form.element(FormatFormElementTestEntity.$.time).setText("1:23");
		form.element(FormatFormElementTestEntity.$.dateTime).setText("1.2.2023 1:23:45.678");

		assertEquals("Date should be formatted", "01.02.2023", form.element(FormatFormElementTestEntity.$.date).getText());
		assertEquals("Time should be formatted", "01:23", form.element(FormatFormElementTestEntity.$.time).getText());
		assertEquals("DateTime should be formatted", "01.02.2023 01:23:45.678", form.element(FormatFormElementTestEntity.$.dateTime).getText());
	}

	
	public static class FormatFormElementTestEntity {
		public static final FormatFormElementTestEntity $ = Keys.of(FormatFormElementTestEntity.class);
		
		public LocalTime time;
		public LocalDate date;
		@Size(Size.TIME_WITH_MILLIS)
		public LocalDateTime dateTime;
	}
	
	public static class FormatFormElementTestApplication extends Application {
		@Override
		public List<Action> getNavigation() {
			return List.of(new FormatFormElementTestEditor());
		}
	}

	public static class FormatFormElementTestEditor extends NewObjectEditor<FormatFormElementTestEntity> {

		@Override
		protected Form<FormatFormElementTestEntity> createForm() {
			Form<FormatFormElementTestEntity> form = new Form<>();
			form.line(FormatFormElementTestEntity.$.time);
			form.line(FormatFormElementTestEntity.$.date);
			form.line(FormatFormElementTestEntity.$.dateTime);
			return form;
		}

		@Override
		protected FormatFormElementTestEntity save(FormatFormElementTestEntity object) {
			return object;
		}
	
	}

}
