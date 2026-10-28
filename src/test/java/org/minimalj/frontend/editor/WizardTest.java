package org.minimalj.frontend.editor;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonPageManager;
import org.minimalj.frontend.page.Page.Dialog;
import org.minimalj.model.Keys;
import org.minimalj.test.TestUtil;

public class WizardTest {

	private static final AtomicInteger shownDialogs = new AtomicInteger();

	@BeforeClass
	public static void setup() {
		Application.setInstance(new Application() {});
		Frontend.setInstance(new JsonFrontend());
		JsonFrontend.setUseInputTypes(false);
		JsonFrontend.setSession(new JsonPageManager() {
			@Override
			public void showDialog(Dialog dialog) {
				shownDialogs.incrementAndGet();
			}

			@Override
			public void closeDialog(Dialog dialog) {
				//
			}
		});
	}

	@AfterClass
	public static void shutdown() {
		TestUtil.shutdown();
	}

	@Test
	public void testRunAgainAfterCancel() {
		TestWizard wizard = new TestWizard();
		wizard.run();
		Assert.assertEquals(1, shownDialogs.get());

		wizard.cancel();
		wizard.run();
		Assert.assertEquals("A canceled wizard must show its dialog again", 2, shownDialogs.get());
	}

	public static class TestWizardObject {
		public static final TestWizardObject $ = Keys.of(TestWizardObject.class);

		public Integer value;
	}

	private static class TestWizard extends Wizard<TestWizardObject> {

		@Override
		protected WizardStep<?> getFirstStep() {
			return new WizardStep<TestWizardObject>() {
				@Override
				public String getTitle() {
					return "Step";
				}

				@Override
				public TestWizardObject createObject() {
					return new TestWizardObject();
				}

				@Override
				public Form<TestWizardObject> createForm() {
					Form<TestWizardObject> form = new Form<>();
					form.line(TestWizardObject.$.value);
					return form;
				}

				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public WizardStep<?> getNextStep() {
					return null;
				}

				@Override
				public WizardStep<?> getPreviousStep() {
					return null;
				}
			};
		}

		@Override
		protected TestWizardObject save() {
			return null;
		}
	}
}
