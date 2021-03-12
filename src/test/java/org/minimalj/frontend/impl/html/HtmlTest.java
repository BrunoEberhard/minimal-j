package org.minimalj.frontend.impl.html;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.minimalj.test.TestUtil;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public abstract class HtmlTest {

	private static RemoteWebDriver driver;
	
	protected static RemoteWebDriver getDriver() {
		return driver;
	}
	
	@BeforeClass
	public static void beforeClass() {
		// https://docs.microsoft.com/en-us/microsoft-edge/webdriver-chromium/?tabs=java
		// https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/
		System.setProperty("webdriver.edge.driver", "C:\\Data\\programme\\selenium_driver\\msedgedriver.exe");
		driver = new EdgeDriver();
	}
	
	@Before
	public void before() {
	}
	
	@After
	public void shutdown() {
		driver.close();
		TestUtil.shutdown();
	}
	
	@AfterClass
	public static void afterClass() {
		driver.quit();
	}
	
}
