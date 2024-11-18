package org.minimalj.test;

import org.junit.jupiter.api.Assertions;
import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.test.PageContainerTestFacade.DialogTestFacade;
import org.minimalj.test.PageContainerTestFacade.FormTestFacade;
import org.minimalj.test.PageContainerTestFacade.TableTestFacade;
import org.minimalj.test.headless.HeadlessTestFacade;
import org.minimalj.test.web.WebTestFacade;

public abstract class UiTest {

	private static UiTestFacade ui;
	private static Application application;

	static {
		if (Configuration.get("UiTest", "").equals("headless")) {
			ui = new HeadlessTestFacade();
		} else {
			ui = new WebTestFacade();
		}
	}

	public static UiTestFacade ui() {
		return ui;
	}

	public static DialogTestFacade dialog() {
		return ui().getCurrentPageContainerTestFacade().getDialog();
	}

	public static void navigate(String action) {
		var pageContainer = ui().getCurrentPageContainerTestFacade();
		pageContainer.getNavigation().run(action);
	}

	public static void contextAction(String caption) {
		var pageContainer = ui().getCurrentPageContainerTestFacade();
		var action = pageContainer.getPage().getContextMenu().get(caption);
		Assertions.assertNotNull(action, "There should be a context menu item '" + caption + "'");
		action.run();
	}

	public static TableTestFacade table() {
		return ui().getCurrentPageContainerTestFacade().getPage().getTable();
	}

	public static FormTestFacade form() {
		return ui().getCurrentPageContainerTestFacade().getPage().getForm();
	}

	public static void start(Application application) {
		if (UiTest.application != application) {
			TestUtil.shutdown();
			Application.setInstance(application);
			UiTest.application = application;
			ui.start(application);
		}
	}

}
