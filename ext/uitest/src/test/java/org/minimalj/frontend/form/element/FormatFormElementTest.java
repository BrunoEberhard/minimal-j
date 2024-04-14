package org.minimalj.frontend.form.element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minimalj.application.Application;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.test.PageContainerTestFacade;
import org.minimalj.test.PageContainerTestFacade.FormTestFacade;
import org.minimalj.test.web.WebTest;

public class FormatFormElementTest extends WebTest {
	
	@Test
	public void test() {
		start(new FormatFormElementTestApplication());

		PageContainerTestFacade window = ui().getCurrentPageContainerTestFacade();
		
		window.getNavigation().get(new FormatFormElementTestEditor().getName()).run();
		
		FormTestFacade form = window.getDialog().getForm();
		form.element(FormatFormElementTestEntity.$.date).setText("1.2.2023");
		form.element(FormatFormElementTestEntity.$.time).setText("1:23");
		form.element(FormatFormElementTestEntity.$.dateTime).setText("1.2.2023 1:23:45.678");

		Assertions.assertEquals("01.02.2023", form.element(FormatFormElementTestEntity.$.date).getText(), "Date should be formatted");
		Assertions.assertEquals("01:23", form.element(FormatFormElementTestEntity.$.time).getText(), "Time should be formatted");
		Assertions.assertEquals("01.02.2023 01:23:45.678", form.element(FormatFormElementTestEntity.$.dateTime).getText(), "DateTime should be formatted");
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
