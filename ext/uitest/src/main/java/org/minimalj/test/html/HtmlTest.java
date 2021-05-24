package org.minimalj.test.html;

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
		TestUtil.shutdown();
	}
	
	@AfterClass
	public static void afterClass() {
		driver.quit();
	}

	public static String escapeXpath(String input) {
		if (input.contains("'") && input.contains("\"")) {
			// this code is never used at the moment
			
			StringBuilder s = new StringBuilder();
			s.append("concat(");

			Character open = null;
			for (int i = 0; i < input.length(); i++) {
				char c = input.charAt(i);
				if (c == '\'') {
					if (open != null && open.equals('"')) {
						s.append(c);
					} else if (open != null && open.equals('\'')) {
						s.append("', \"'");
						open = '"';
					} else {
						s.append("\"'");
						open = '"';
					}
				} else if (c == '"') {
					if (open != null && open.equals('\'')) {
						s.append(c);
					} else if (open != null && open.equals('\"')) {
						s.append("\", '\"");
						open = '\'';
					} else {
						s.append("'\"");
						open = '\'';
					}
				} else {
					if (open == null) {
						s.append("\"").append(c);
						open = '"';
					} else {
						s.append(c);
					}
				}
			}

			if (open.equals('\'')) {
				s.append("')");
			} else if (open.equals('\"')) {
				s.append("\")");
			}
			
			return s.toString();
		} else if (input.contains("'")) {
			return "\"" + input + "\"";
		} else {
			return "'" + input + "'";
		}
	}
}
