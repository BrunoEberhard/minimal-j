package org.minimalj.frontend.impl.json;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.minimalj.application.Application.AuthenticatonMode;
import org.minimalj.frontend.impl.swing.SwingAuthenticationTest.TestApplication;
import org.minimalj.frontend.impl.web.WebServer;
import org.minimalj.test.TestUtil;
import org.minimalj.util.resources.Resources;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;

@Ignore("Not working on travis")
public class JsonAuthenticationTest {

	private EdgeDriver driver;
	
	@BeforeClass
	public static void beforeClass() {
		// https://docs.microsoft.com/en-us/microsoft-edge/webdriver-chromium/?tabs=java
		// https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/
		System.setProperty("webdriver.edge.driver", "C:\\Data\\programme\\selenium_driver\\msedgedriver.exe");
	}
	
	@Before
	public void before() {
		driver = new EdgeDriver();
	}
	
	@After
	public void shutdown() {
		driver.close();
		TestUtil.shutdown();
	}
	
	@Test
	public void testAuthenticatonModeRequired() {
		WebServer.start(new TestApplication(AuthenticatonMode.REQUIRED));
		driver.get("http://localhost:8080");
		waitBlock();
		loginShouldBeShown();
		startWithoutLoginShouldBePossible();
		
		setText("UserPassword.user", "test");
		setText("UserPassword.password", "test");
		
		clickButton("LoginAction");
		
		textShouldBeDisplayed("TestPage");
		textShouldBeDisplayed("Subject: test");
		textShouldBeDisplayed("Action with login");
		shouldBeVisible("logout", true);
		shouldBeVisible("login", false);
	
		driver.executeScript(driver.findElementById("logout").getAttribute("onclick"));
		waitBlock();
		
		loginShouldBeShown();
		textShouldBeDisplayed("Action without login");
	}
	
	@Test
	public void testAuthenticatonModeSuggested() {
		WebServer.start(new TestApplication(AuthenticatonMode.SUGGESTED));
		driver.get("http://localhost:8080");
		waitBlock();
		loginShouldBeShown();
		noLoginButtonShouldBeShown();
	}

	@Test
	public void testAuthenticatonModeOptional() {
		WebServer.start(new TestApplication(AuthenticatonMode.OPTIONAL));
		driver.get("http://localhost:8080");
		waitBlock();
		textShouldBeDisplayed("Subject: -");
	}

	@Test
	public void testAuthenticatonModeNotAvailable() {
		WebServer.start(new TestApplication(AuthenticatonMode.NOT_AVAILABLE));
		driver.get("http://localhost:8080");
		waitBlock();
		textShouldBeDisplayed("Subject: -");
		shouldBeVisible("logout", false);
		shouldBeVisible("login", false);
	}

	private void waitBlock() {
		long start = System.currentTimeMillis();
		WebElement block = driver.findElementByClassName("is-blocked");
		while (true) {
			if (System.currentTimeMillis() - start > 10*1000) {
				throw new RuntimeException("timeout");
			}
			if (block.getCssValue("height").equals("0px")) {
				return;
			}
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				throw new RuntimeException();
			}
		}
	}
	
	private void setText(String resourceName, String text) {
		String caption = Resources.getString(resourceName);
		WebElement label = driver.findElement(By.xpath("//label[text()='" + caption + "']"));
		String id = label.getAttribute("for");
		WebElement element = driver.findElementById(id);
		element.clear();
		element.sendKeys(text);
	}
	
	private void clickButton(String resourceName) {
		String caption = Resources.getString(resourceName);
		WebElement element = driver.findElement(By.xpath("//button[text()='" + caption + "']"));
		driver.executeScript(element.getAttribute("onclick"));
		// element.click();
		waitBlock();
	}
	
	private void loginShouldBeShown() {
		Assert.assertEquals(Resources.getString("Login.title"), driver.getTitle());
	}

	private void noLoginButtonShouldBeShown() {
		Assert.assertNotNull(driver.findElement(By.xpath("//button[text()='" + Resources.getString("SkipLoginAction") + "']")));
	}

	private void startWithoutLoginShouldBePossible() {
		Assert.assertTrue(driver.findElements(By.xpath("//button[text()='" + Resources.getString("SkipLoginAction") + "']")).isEmpty());
	}
	
	private void shouldBeVisible(String id, boolean visible) {
		Assert.assertEquals(visible, !"none".equals(driver.findElementById(id).getCssValue("display")));	
	}

	private void textShouldBeDisplayed(String text) {
		Assert.assertTrue(text + " should be displayed", driver.getPageSource().contains(text));
//		WebElement iframeElement = driver.findElement(By.xpath("//iframe"));        
//		driver.switchTo().frame(iframeElement);
//		
//		Assert.assertTrue(text + " should be displayed", driver.findElement(By.xpath("//*[text()='"+ text + "']")) != null);
//		
//		driver.switchTo().parentFrame();
	}
}
