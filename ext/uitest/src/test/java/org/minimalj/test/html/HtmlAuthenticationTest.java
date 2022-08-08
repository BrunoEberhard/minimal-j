package org.minimalj.test.html;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.minimalj.application.Application.AuthenticatonMode;
import org.minimalj.test.LoginFrameFacade.UserPasswordLoginTestFacade;
import org.minimalj.test.PageContainerTestFacade;
import org.minimalj.test.PageContainerTestFacade.PageTestFacade;
import org.minimalj.test.TestApplication;
import org.minimalj.test.TestUtil;
import org.minimalj.test.web.WebTest;

public class HtmlAuthenticationTest extends WebTest {

	@After
	public void cleanup() {
		TestUtil.shutdown();
	}
	
	@Test
	public void testAuthenticatonModeRequired() {
		start(new TestApplication(AuthenticatonMode.REQUIRED));

		login();
		
		PageContainerTestFacade pageContainer = ui().getCurrentPageContainerTestFacade();

		PageTestFacade page = pageContainer.getPage();
		Assert.assertTrue(page.contains("TestPage"));
		Assert.assertTrue(page.contains("Subject: test"));
		
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
		Assert.assertEquals(TestApplication.TEST_PAGE_TITLE, pageContainer.getPage().getTitle());
		pageContainer.logout();
		
		login();

		pageContainer = ui().getCurrentPageContainerTestFacade();
		Assert.assertEquals(TestApplication.TEST_PAGE_TITLE, pageContainer.getPage().getTitle());
		pageContainer.logout();
	}

	@Test
	public void testBackAfterLogout() {
		start(new TestApplication(AuthenticatonMode.REQUIRED));
		
		login();

		PageContainerTestFacade pageContainer = ui().getCurrentPageContainerTestFacade();
		PageTestFacade page = pageContainer.getPage();
		Assert.assertTrue(page.contains("TestPage"));

		ui().getCurrentPageContainerTestFacade().getNavigation().get("Other Page").run();
		page = pageContainer.getPage();
		Assert.assertTrue(page.contains("Page 2"));

		pageContainer.logout();
		
		ui().getCurrentPageContainerTestFacade().getBack().run();

		login();

		page = pageContainer.getPage();
		Assert.assertTrue(page.contains("Page 2"));

		pageContainer.logout();
	}
	
	@Test
	public void testAuthenticatonModeSuggested() {
		start(new TestApplication(AuthenticatonMode.SUGGESTED));
		
		Assert.assertTrue(ui().getLoginTestFacade().hasSkipLogin());
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
//		Assert.assertEquals(Resources.getString("Login.title"), driver.getTitle());
//	}
//
//
	//	private void startWithoutLoginShouldBePossible() {
//		Assert.assertTrue(driver.findElements(By.xpath("//button[text()='" + Resources.getString("SkipLoginAction") + "']")).isEmpty());
//	}
//	
//	private void shouldBeVisible(String id, boolean visible) {
//		Assert.assertEquals(visible, !"none".equals(driver.findElementById(id).getCssValue("display")));	
//	}
//
//	private void textShouldBeDisplayed(String text) {
//		Assert.assertTrue(text + " should be displayed", driver.getPageSource().contains(text));
//		WebElement iframeElement = driver.findElement(By.xpath("//iframe"));        
//		driver.switchTo().frame(iframeElement);
//		
//		Assert.assertTrue(text + " should be displayed", driver.findElement(By.xpath("//*[text()='"+ text + "']")) != null);
//		
//		driver.switchTo().parentFrame();
//	}
}
