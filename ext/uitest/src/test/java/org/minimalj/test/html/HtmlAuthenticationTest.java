package org.minimalj.test.html;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minimalj.application.Application.AuthenticatonMode;
import org.minimalj.test.LoginFrameFacade.UserPasswordLoginTestFacade;
import org.minimalj.test.PageContainerTestFacade;
import org.minimalj.test.PageContainerTestFacade.PageTestFacade;
import org.minimalj.test.TestApplication;
import org.minimalj.test.TestUtil;
import org.minimalj.test.UiTest;

public class HtmlAuthenticationTest extends UiTest {

	@AfterEach
	public void cleanup() {
		TestUtil.shutdown();
	}
	
	@Test
	public void testAuthenticatonModeRequired() {
		start(new TestApplication(AuthenticatonMode.REQUIRED));

		login();
		
		PageContainerTestFacade pageContainer = ui().getCurrentPageContainerTestFacade();

		PageTestFacade page = pageContainer.getPage();
		Assertions.assertTrue(page.contains("TestPage"));
		Assertions.assertTrue(page.contains("Subject: test"));
		
		pageContainer.logout();
	}

	private void login() {
		UserPasswordLoginTestFacade userPasswordLogin = ui().getLoginTestFacade();
		userPasswordLogin.setUser("test");
		userPasswordLogin.setPassword("test");
		userPasswordLogin.login();
	}
	
	@Test
	public void testRelogin() {
		start(new TestApplication(AuthenticatonMode.REQUIRED));
		
		login();

		PageContainerTestFacade pageContainer = ui().getCurrentPageContainerTestFacade();
		Assertions.assertEquals(TestApplication.TEST_PAGE_TITLE, pageContainer.getPage().getTitle());
		pageContainer.logout();
		
		login();

		pageContainer = ui().getCurrentPageContainerTestFacade();
		Assertions.assertEquals(TestApplication.TEST_PAGE_TITLE, pageContainer.getPage().getTitle());
		pageContainer.logout();
	}

	@Test
	public void testBackAfterLogout() {
		start(new TestApplication(AuthenticatonMode.REQUIRED));
		
		login();

		PageContainerTestFacade pageContainer = ui().getCurrentPageContainerTestFacade();
		PageTestFacade page = pageContainer.getPage();
		Assertions.assertTrue(page.contains("TestPage"));

		ui().getCurrentPageContainerTestFacade().getNavigation().get("Other Page").run();
		page = pageContainer.getPage();
		Assertions.assertTrue(page.contains("Page 2"));

		pageContainer.logout();
		
		var back = ui().getCurrentPageContainerTestFacade().getBack();
		if (back.isEnabled()) {
			back.run();

			page = pageContainer.getPage();
			Assertions.assertTrue(page.contains("refresh"), "User cannot return to previous page after logout");
		}
	}
	
	@Test
	public void testAuthenticatonModeSuggested() {
		start(new TestApplication(AuthenticatonMode.SUGGESTED));
		
		Assertions.assertTrue(ui().getLoginTestFacade().hasSkipLogin());
	}
}
