package org.minimalj.frontend.impl.html;

import java.util.List;

import org.junit.Assert;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.test.ApplicationTestFacade;
import org.minimalj.frontend.test.FrameTestFacade.PageContainerTestFacade;
import org.minimalj.frontend.test.FrameTestFacade.UserPasswordLoginTestFacade;
import org.minimalj.util.resources.Resources;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class HtmlTestFacade implements ApplicationTestFacade {

	private final RemoteWebDriver driver;
	
	static {
		// https://docs.microsoft.com/en-us/microsoft-edge/webdriver-chromium/?tabs=java
		// https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/
		System.setProperty("webdriver.edge.driver", "C:\\Data\\programme\\selenium_driver\\msedgedriver.exe");
	}
	
	public HtmlTestFacade(RemoteWebDriver driver) {
		this.driver = driver;
		
		String portString = Configuration.get("MjFrontendPort", "8080");
		driver.get("http://localhost:" + portString);
		waitBlock();
	}
	
	public void shutdown() {
		driver.close();
	}

	@Override
	public UserPasswordLoginTestFacade getLoginTestFacade() {
		Assert.assertEquals(Resources.getString("Login.title"), driver.getTitle());
		
		return new HtmlLoginTestFacade();
	}
	
	public class HtmlLoginTestFacade implements UserPasswordLoginTestFacade {

		@Override
		public boolean hasSkipLogin() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean hasClose() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void login() {
			clickButton("LoginAction");
		}

		@Override
		public void cancel() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void close() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setUser(String name) {
			setText("UserPassword.user", name);
		}

		@Override
		public void setPassword(String password) {
			setText("UserPassword.password", password);
		}
		
	}

	private class HtmlPageContainerTestFacade implements PageContainerTestFacade {
		private int forwards = 0;
		
		@Override
		public NavigationTestFacade getNavigation() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<PageTestFacade> getPages() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ActionTestFacade getBack() {
			return new BackTestFacade();
		}

		@Override
		public ActionTestFacade getForward() {
			return new FowardTestFacade();
		}
	
		private class BackTestFacade implements ActionTestFacade {
			@Override
			public void run() {
				driver.navigate().back();
				forwards++;
			}
			
			@Override
			public boolean isEnabled() {
				return (boolean) driver.executeScript("return window.history.length > 0;");
			}
		}
		
		private class FowardTestFacade implements ActionTestFacade {
			@Override
			public void run() {
				driver.navigate().forward();
				forwards--;
			}
			
			@Override
			public boolean isEnabled() {
				return forwards > 0;
			}
		}
		
	}
	
	@Override
	public PageContainerTestFacade getCurrentWindowTestFacade() {
		// TODO Auto-generated method stub
		return null;
	}
	

	//
	
	private void clickButton(String resourceName) {
		String caption = Resources.getString(resourceName);
		WebElement element = driver.findElement(By.xpath("//button[text()='" + caption + "']"));
		driver.executeScript(element.getAttribute("onclick"));
		// element.click();
		waitBlock();
	}

	private void setText(String resourceName, String text) {
		String caption = Resources.getString(resourceName);
		WebElement label = driver.findElement(By.xpath("//label[text()='" + caption + "']"));
		String id = label.getAttribute("for");
		WebElement element = driver.findElementById(id);
		element.clear();
		element.sendKeys(text);
	}

	private void waitBlock() {
		long start = System.currentTimeMillis();
		WebElement block = driver.findElementByClassName("is-blocked");
		while (true) {
			if (System.currentTimeMillis() - start > 10 * 1000) {
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
}
