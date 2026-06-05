package org.minimalj.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.test.PageContainerTestFacade.DialogTestFacade;
import org.minimalj.test.PageContainerTestFacade.FormTestFacade;
import org.minimalj.test.PageContainerTestFacade.TableTestFacade;
import org.minimalj.test.headless.HeadlessTestFacade;
import org.minimalj.test.playwright.PlaywrightTestFacade;
import org.minimalj.test.web.SeleniumTestFacade;
import org.minimalj.util.StringUtils;

public abstract class UiTest {
	public static final String CONFIGURATION_UI_TEST_BROWSER = "UiTestBrowser";
	public static final String CONFIGURATION_UI_TEST_ENGINE = "UiTestEngine";
	public static final String CONFIGURATION_UI_TEST_HEADLESS = "UiTestHeadless";

	public enum UiTestEngine {
		selenium, playwright, minimal;
	}
	
	public enum UiTestBrowser {
		firefox, chrome;
	}
	
	private static UiTestFacade ui;
	private static Application application;

	static {
		UiTestEngine engine = UiTestEngine.minimal;
		String configurationUiTestEngine = Configuration.get(CONFIGURATION_UI_TEST_ENGINE);
		if (!StringUtils.isEmpty(configurationUiTestEngine)) {
			try {
				engine = UiTestEngine.valueOf(configurationUiTestEngine);
			} catch (Exception x) {
				throw new IllegalArgumentException("Invalid " + CONFIGURATION_UI_TEST_ENGINE + ": " + configurationUiTestEngine);
			}
		}
		
		UiTestBrowser uiTestBrowser = UiTestBrowser.firefox;
		String configurationUiTestDriver = Configuration.get(CONFIGURATION_UI_TEST_BROWSER);
		if (!StringUtils.isEmpty(configurationUiTestDriver)) {
			try {
				uiTestBrowser = UiTestBrowser.valueOf(configurationUiTestDriver);
			} catch (Exception x) {
				throw new IllegalArgumentException("Invalid " + CONFIGURATION_UI_TEST_BROWSER + ": " + configurationUiTestDriver);
			}
		}

		boolean headless = "true".equals(Configuration.get(CONFIGURATION_UI_TEST_HEADLESS, "false"));
		if (engine == UiTestEngine.playwright) {
			ui = new PlaywrightTestFacade(uiTestBrowser, headless);
		} else if (engine == UiTestEngine.selenium) {
			ui = new SeleniumTestFacade(uiTestBrowser, headless);
		} else {
			ui = new HeadlessTestFacade();
		}
	}
	
	@AfterEach
	public void closeAllDialogs() {
		if (Frontend.isAvailable()) {
			// if a test fails and a dialog stays open the coming tests would fail.
			// therefore close all open dialogs at the end of each test.
			var dialog = dialog();
			while (dialog != null) {
				dialog.close();
				dialog = dialog();
			}
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
		if (isStarted() && UiTest.application != application) {
			TestUtil.shutdown();
		}
		Application.setInstance(application);
		UiTest.application = application;
		ui.start(application);
	}

	public static boolean isStarted() {
		return application != null;
	}
}
