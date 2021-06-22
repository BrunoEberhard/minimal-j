package org.minimalj.test.html;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.application.Application.AuthenticatonMode;
import org.minimalj.frontend.impl.web.WebServer;
import org.minimalj.test.ApplicationTestFacade;
import org.minimalj.test.LoginFrameFacade.UserPasswordLoginTestFacade;
import org.minimalj.test.PageContainerTestFacade;
import org.minimalj.test.PageContainerTestFacade.PageTestFacade;
import org.minimalj.test.TestApplication;

public class HtmlAuthenticationTest extends HtmlTest {

	@Test
	public void testAuthenticatonModeRequired() {
		WebServer.start(new TestApplication(AuthenticatonMode.REQUIRED));

		ApplicationTestFacade application = new HtmlTestFacade(getDriver());

		UserPasswordLoginTestFacade userPasswordLogin = application.getLoginTestFacade();

		userPasswordLogin.setUser("test");
		userPasswordLogin.setPassword("test");

		userPasswordLogin.login();

		PageContainerTestFacade pageContainer = application.getCurrentPageContainerTestFacade();

		PageTestFacade page = pageContainer.getPage();
		Assert.assertTrue(page.contains("TestPage"));
		Assert.assertTrue(page.contains("Subject: test"));

		
		pageContainer.logout();
	}

//	@Test
//	public void testAuthenticatonModeSuggested() {
//		WebServer.start(new TestApplication(AuthenticatonMode.SUGGESTED));
//		driver.get("http://localhost:8080");
//		waitBlock();
//		loginShouldBeShown();
//		noLoginButtonShouldBeShown();
//	}
//
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
//	private void noLoginButtonShouldBeShown() {
//		Assert.assertNotNull(driver.findElement(By.xpath("//button[text()='" + Resources.getString("SkipLoginAction") + "']")));
//	}
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
