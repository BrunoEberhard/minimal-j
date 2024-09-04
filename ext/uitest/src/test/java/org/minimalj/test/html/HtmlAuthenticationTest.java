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
import org.minimalj.test.web.WebTest;

public class HtmlAuthenticationTest extends WebTest {

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
		
		ui().getCurrentPageContainerTestFacade().getBack().run();

		page = pageContainer.getPage();
		Assertions.assertTrue(page.contains("refresh"), "User cannot return to previous page after logout");
	}
	
	@Test
	public void testAuthenticatonModeSuggested() {
		start(new TestApplication(AuthenticatonMode.SUGGESTED));
		
		Assertions.assertTrue(ui().getLoginTestFacade().hasSkipLogin());
	}

//	@Test
//	public void testAuthenticatonModeOptional() {
//		WebServer.start(new TestApplication(AuthenticatonMode.OPTIONAL));
//		driver.get("http://localhost:8080");
//		waitBlock();
//		textShouldBeDisplayed("Subject: -");
//	}
//
//	@Test
//	public void testAuthenticatonModeNotAvailable() {
//		WebServer.start(new TestApplication(AuthenticatonMode.NOT_AVAILABLE));
//		driver.get("http://localhost:8080");
//		waitBlock();
//		textShouldBeDisplayed("Subject: -");
//		shouldBeVisible("logout", false);
//		shouldBeVisible("login", false);
//	}
//
//	private void waitBlock() {
//		long start = System.currentTimeMillis();
//		WebElement block = driver.findElementByClassName("is-blocked");
//		while (true) {
//			if (System.currentTimeMillis() - start > 10*1000) {
//				throw new RuntimeException("timeout");
//			}
//			if (block.getCssValue("height").equals("0px")) {
//				return;
//			}
//			try {
//				Thread.sleep(5);
//			} catch (InterruptedException e) {
//				throw new RuntimeException();
//			}
//		}
//	}
//	
//	private void loginShouldBeShown() {
//		Assertions.assertEquals(Resources.getString("Login.title"), driver.getTitle());
//	}
//
//
	//	private void startWithoutLoginShouldBePossible() {
//		Assertions.assertTrue(driver.findElements(By.xpath("//button[text()='" + Resources.getString("SkipLoginAction") + "']")).isEmpty());
//	}
//	
//	private void shouldBeVisible(String id, boolean visible) {
//		Assertions.assertEquals(visible, !"none".equals(driver.findElementById(id).getCssValue("display")));	
//	}
//
//	private void textShouldBeDisplayed(String text) {
//		Assertions.assertTrue(text + " should be displayed", driver.getPageSource().contains(text));
//		WebElement iframeElement = driver.findElement(By.xpath("//iframe"));        
//		driver.switchTo().frame(iframeElement);
//		
//		Assertions.assertTrue(text + " should be displayed", driver.findElement(By.xpath("//*[text()='"+ text + "']")) != null);
//		
//		driver.switchTo().parentFrame();
//	}
}
