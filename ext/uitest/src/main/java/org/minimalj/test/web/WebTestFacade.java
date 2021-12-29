package org.minimalj.test.web;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.minimalj.application.Configuration;
import org.minimalj.test.LoginFrameFacade.UserPasswordLoginTestFacade;
import org.minimalj.test.PageContainerTestFacade;
import org.minimalj.test.PageContainerTestFacade.ActionTestFacade;
import org.minimalj.test.PageContainerTestFacade.DialogTestFacade;
import org.minimalj.test.PageContainerTestFacade.FormElementTestFacade;
import org.minimalj.test.PageContainerTestFacade.FormTestFacade;
import org.minimalj.test.PageContainerTestFacade.NavigationTestFacade;
import org.minimalj.test.PageContainerTestFacade.PageTestFacade;
import org.minimalj.test.PageContainerTestFacade.SearchTableTestFacade;
import org.minimalj.test.PageContainerTestFacade.TableTestFacade;
import org.minimalj.test.UiTestFacade;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebTestFacade implements UiTestFacade {

	private final RemoteWebDriver driver = createDriver();

	private static final String WEBDRIVER_EDGE_DRIVER = "webdriver.edge.driver";
	
	private static RemoteWebDriver createDriver() {
		// https://docs.microsoft.com/en-us/microsoft-edge/webdriver-chromium/?tabs=java
		// https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/
		System.setProperty(WEBDRIVER_EDGE_DRIVER, "C:\\Data\\programme\\selenium_driver\\msedgedriver.exe");
		RemoteWebDriver driver = new EdgeDriver();
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> driver.quit()));
		return driver;
	}

	public WebTestFacade() {
		reload();
	}

	public void reload() {
		String portString = Configuration.get("MjFrontendPort", "8080");
		driver.get("http://localhost:" + portString);
		waitScript();
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
			setText(driver.findElement(By.id("pageContainer")), Resources.getString("UserPassword.user"), name);
		}

		@Override
		public void setPassword(String password) {
			setText(driver.findElement(By.id("pageContainer")), Resources.getString("UserPassword.password"), password);
		}
	}
	
	@Override
	public void logout() {
		WebElement element = driver.findElement(By.id("logout"));
		driver.executeScript(element.getAttribute("onclick"));
		waitScript();
	}

	private class HtmlPageContainerTestFacade implements PageContainerTestFacade {
		private int forwards = 0;

		@Override
		public NavigationTestFacade getNavigation() {
			return new HtmlNavigationTestFacade();
		}

		@Override
		public List<PageTestFacade> getPages() {
			WebElement divPageContainer = driver.findElement(By.id("pageContainer"));
			List<PageTestFacade> pages = new ArrayList<>();
			for (WebElement divPage : divPageContainer.findElements(By.className("page"))) {
				pages.add(new HtmlPageTestFacade(divPage));
			}
			return pages;
		}

		@Override
		public DialogTestFacade getDialog() {
			List<WebElement> dialogs = driver.findElements(By.tagName("dialog"));
			return new HtmlDialogTestFacade(dialogs.get(dialogs.size() - 1));
		}

		@Override
		public ActionTestFacade getBack() {
			return new BackTestFacade();
		}

		@Override
		public ActionTestFacade getForward() {
			return new FowardTestFacade();
		}

		@Override
		public boolean hasLogout() {
			WebElement logout = driver.findElement(By.id("logout"));
			return logout != null && logout.isDisplayed();
		}
		
		@Override
		public void logout() {
			WebElement logout = driver.findElement(By.id("logout"));
			driver.executeScript(logout.getAttribute("onclick"));
			waitScript();
		}
		
		private class HtmlNavigationTestFacade implements NavigationTestFacade {

			@Override
			public Runnable get(String text) {
				WebElement divNavigation = driver.findElement(By.id("navigation"));
				WebElement item = divNavigation.findElement(By.xpath(".//a[text()=" + WebTest.escapeXpath(text) + "]"));
				return () -> {
					if (!divNavigation.isDisplayed()) {
						WebElement navigationToggle = driver.findElement(By.id("navigationToggle"));
						navigationToggle.click();
						WebDriverWait webDriverWait = new WebDriverWait(driver, 10);
						webDriverWait.until(ExpectedConditions.elementToBeClickable(item));
					}
					item.click();
					waitScript();
				};
			}

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

	private class HtmlPageTestFacade implements PageTestFacade {
		private final WebElement divPage;

		public HtmlPageTestFacade(WebElement divPage) {
			this.divPage = divPage;
		}

		@Override
		public void executeQuery(String query) {
			try {
				WebElement inputQuerySearch = divPage.findElement(By.id("querySearch"));
				inputQuerySearch.sendKeys(query);
				inputQuerySearch.sendKeys("\n");
				waitScript();
			} catch (NoSuchElementException e) {
				throw new IllegalStateException("Page is not a query page", e);
			}
		}

		@Override
		public String getTitle() {
			WebElement spanPageTitle = divPage.findElement(By.className("pageTitle"));
			// spanPageTitle.getText() only works if title is visible (which it is not on
			// small screens)
			return spanPageTitle.getAttribute("textContent");
		}

		@Override
		public TableTestFacade getTable() {
			try {
				WebElement table = divPage.findElement(By.className("table"));
				return new HtmlTableTestFacade(table);
			} catch (NoSuchElementException e) {
				throw new IllegalStateException("Page is not a table page", e);
			}
		}

		@Override
		public FormTestFacade getForm() {
			WebElement form = divPage.findElement(By.className("form"));
			return new HtmlFormTestFacade(form);
		}

		@Override
		public boolean contains(String string) {
			WebElement iframe = divPage.findElement(By.tagName("iframe"));
			driver.switchTo().frame(iframe);
			WebElement body = driver.findElement(By.tagName("body"));
			boolean contains = body.getText().contains(string); 
			driver.switchTo().defaultContent();
			return contains;
		}

		@Override
		public NavigationTestFacade getContextMenu() {
			return new PageContextMenuTestFacade(divPage);
		}
	}

	private class PageContextMenuTestFacade implements NavigationTestFacade {
		private final WebElement divPage;

		public PageContextMenuTestFacade(WebElement divPage) {
			this.divPage = divPage;
		}

		@Override
		public Runnable get(String text) {
			return () -> {
				WebElement actionMenu = divPage.findElement(By.className("actionMenu"));
				WebElement item;
				if (actionMenu.isDisplayed()) {
					item = actionMenu.findElement(By.xpath(".//*[text()=" + WebTest.escapeXpath(text) + "]"));
				} else {
					WebElement actionMenuButton = divPage.findElement(By.className("actionMenuButton"));
					if (!actionMenuButton.isDisplayed()) {
						actionMenuButton = driver.findElementById("actionMenuButton");
					}
					actionMenuButton.click();
					item = divPage.findElement(By.xpath(".//*[text()=" + WebTest.escapeXpath(text) + "]"));
				}
				item.click();
				waitScript();
			};
		}
	}
	
	private class HtmlDialogTestFacade implements DialogTestFacade {
		private final WebElement dialog;

		public HtmlDialogTestFacade(WebElement dialog) {
			this.dialog = dialog;
		}

		@Override
		public FormTestFacade getForm() {
			WebElement form = dialog.findElement(By.className("form"));
			return new HtmlFormTestFacade(form);
		}

		@Override
		public ActionTestFacade getAction(String caption) {
			WebElement button = dialog.findElement(By.xpath(".//button[text()=" + WebTest.escapeXpath(caption) + "]"));
			return new HtmlActionTestFacade(button);
		}
	}

	private class HtmlActionTestFacade implements ActionTestFacade {
		private final WebElement button;

		public HtmlActionTestFacade(WebElement button) {
			this.button = button;
		}

		@Override
		public void run() {
			button.click();
			waitScript();
		}

		@Override
		public boolean isEnabled() {
			return button.getAttribute("disabled") == null;
		}
	}

	private class HtmlFormTestFacade implements FormTestFacade {
		private final WebElement form;

		public HtmlFormTestFacade(WebElement form) {
			this.form = form;
		}

		@Override
		public FormElementTestFacade getElement(String caption) {
			WebElement label = form.findElement(By.xpath(".//label[text()=" + WebTest.escapeXpath(caption) + "]"));
			String id = label.getAttribute("for");
			WebElement element = form.findElement(By.id(id));
			return new HtmlFormElementTestFacade(element);
		}
		
		@Override
		public FormElementTestFacade getElement(int row, int column) {
			WebElement rowElement = form.findElements(By.xpath("./div")).get(row);
			WebElement element = rowElement.findElements(By.xpath("./div")).get(column);
			return new HtmlFormElementTestFacade(element);
		}
	}

	private class HtmlFormElementTestFacade implements FormElementTestFacade {
		private final WebElement formElement;

		public HtmlFormElementTestFacade(WebElement formElement) {
			this.formElement = formElement;
		}

		@Override
		public String getText() {
			if (formElement.getTagName().equalsIgnoreCase("select")) {
				Select select = new Select(formElement);
				select.getAllSelectedOptions();
				return select.getAllSelectedOptions().isEmpty() ? null : select.getFirstSelectedOption().getText();
			} else {
				return formElement.getAttribute("value");
			}
		}

		@Override
		public void setText(String value) {
			WebTestFacade.this.setText(formElement, value);
			waitScript();
		}
		
		@Override
		public boolean isChecked() {
			WebElement element = formElement;
			if (!element.getTagName().equalsIgnoreCase("input")) {
				element = element.findElement(By.xpath(".//input"));
			}
			return element.getAttribute("checked") != null;
		}

		@Override
		public void setChecked(boolean checked) {
			WebElement element = formElement;
			if (!element.getTagName().equalsIgnoreCase("input")) {
				element = element.findElement(By.xpath(".//input"));
			}
			boolean isChecked = isChecked();
			if (isChecked != checked) {
				element.click();
			}
			waitScript();
		}
		
		@Override
		public List<String> getComboBoxValues() {
			Select select = new Select(formElement);
			List<WebElement> options = select.getOptions();
			List<String> texts = new ArrayList<>(options.size());
			for (WebElement option: options) {
				texts.add(option.getText());
			}
			return texts;
		}
		
		@Override
		public String getValidation() {
			String id = formElement.getAttribute("id");
			WebElement validationElement = driver.findElementById(id + "-validation");
			if (validationElement.isDisplayed()) {
				String validation = validationElement.getAttribute("title");
				return StringUtils.isEmpty(validation) ? null : validation;
			} else {
				return null;
			}
		}

		@Override
		public SearchTableTestFacade lookup() {
			WebElement lookupButton = formElement.findElement(By.className("lookupbutton"));
			driver.executeScript(lookupButton.getAttribute("onclick"));
			waitScript();
			List<WebElement> dialogs = driver.findElements(By.tagName("dialog"));
			return new HtmlSearchTableTestFacade(dialogs.get(dialogs.size() - 1));
		}

		@Override
		public String getLine(int line) {
			WebElement divGroupVertical = formElement.findElement(By.className("groupVertical"));
			WebElement divGroupItem = divGroupVertical.findElements(By.className("groupItem")).get(line);
			WebElement divText = divGroupItem.findElement(By.className("text"));
			return divText.getText();
		}

		@Override
		public List<ActionTestFacade> getLineActions(int line) {
			WebElement divGroupVertical = formElement.findElement(By.className("groupVertical"));
			WebElement divGroupItem = divGroupVertical.findElements(By.className("groupItem")).get(line);
			WebElement divDropdown = divGroupItem.findElement(By.className("dropdown"));
			List<WebElement> divActions = divDropdown.findElements(By.tagName("div"));
			return divActions.stream().map(this::lineAction).collect(Collectors.toList());
		}

		private ActionTestFacade lineAction(WebElement divAction) {
			return new ActionTestFacade() {
				@Override
				public void run() {
					driver.executeScript(divAction.getAttribute("onclick"));
					waitScript();
				}

				@Override
				public boolean isEnabled() {
					return true;
				}

				@Override
				public String toString() {
					return divAction.getText();
				}
			};
		}
		
		@Override
		public FormElementTestFacade groupItem(int pos) {
			WebElement groupItemElement = formElement.findElements(By.xpath("./div/div")).get(pos);
			return new HtmlFormElementTestFacade(groupItemElement);
		}
	}

	private class HtmlTableTestFacade implements TableTestFacade {
		protected final WebElement table;

		public HtmlTableTestFacade(WebElement table) {
			this.table = table;
		}

		@Override
		public int getColumnCount() {
			WebElement thead = table.findElement(By.tagName("thead"));
			return thead.findElements(By.cssSelector("th.col")).size();
		}

		@Override
		public int getRowCount() {
			WebElement tbody = table.findElement(By.tagName("tbody"));
			return tbody.findElements(By.tagName("tr")).size();
		}

		@Override
		public String getHeader(int column) {
			WebElement thead = table.findElement(By.tagName("thead"));
			return thead.findElements(By.cssSelector("tr.headers th")).get(column).getText();
		}

		@Override
		public String getValue(int row, int column) {
			WebElement tbody = table.findElement(By.tagName("tbody"));
			WebElement tr = tbody.findElements(By.tagName("tr")).get(row);
			return tr.findElements(By.tagName("td")).get(column).getText();
		}

		@Override
		public void activate(int row, int column) {
			WebElement tbody = table.findElement(By.tagName("tbody"));
			WebElement tr = tbody.findElements(By.tagName("tr")).get(row);
			WebElement td = tr.findElements(By.tagName("td")).get(column);
			Actions action = new Actions(driver);
			action.click(td).perform();
			waitScript();
		}

		@Override
		public void activate(int row) {
			WebElement tbody = table.findElement(By.tagName("tbody"));
			WebElement tr = tbody.findElements(By.tagName("tr")).get(row);
			Actions action = new Actions(driver);
			// TODO understand and fix this 'td'
			action.doubleClick(tr.findElement(By.tagName("td"))).perform();
			waitScript();
		}

		@Override
		public void select(int row) {
			WebElement tbody = table.findElement(By.tagName("tbody"));
			WebElement tr = tbody.findElements(By.tagName("tr")).get(row);
			Actions action = new Actions(driver);
			// TODO understand and fix this 'td'
			action.click(tr.findElement(By.tagName("td"))).perform();
			waitScript();
		}
		
		@Override
		public FormTestFacade getFilter() {
			WebElement form = table.findElement(By.cssSelector(".form"));
			return new HtmlFormTestFacade(form);
		}
	}

	private class HtmlSearchTableTestFacade extends HtmlTableTestFacade implements SearchTableTestFacade {

		public HtmlSearchTableTestFacade(WebElement dialog) {
			super(dialog);
			Assert.assertEquals("SearchDialog", dialog.getAttribute("type"));
		}

		public void search(String text) {
			WebElement input = table.findElement(By.tagName("input"));
			setText(input, text);
			WebElement button = table.findElement(By.tagName("button"));
			button.click();
			waitScript();
		}

	}

	@Override
	public PageContainerTestFacade getCurrentPageContainerTestFacade() {
		return new HtmlPageContainerTestFacade();
	}

	//

	private void clickButton(String resourceName) {
		String caption = Resources.getString(resourceName);
		WebElement element = driver.findElement(By.xpath(".//button[text()=" + WebTest.escapeXpath(caption) + "]"));
		driver.executeScript(element.getAttribute("onclick"));
		waitScript();
	}

	private void setText(WebElement container, String caption, String text) {
		WebElement label = container.findElement(By.xpath(".//label[text()=" + WebTest.escapeXpath(caption) + "]"));
		String id = label.getAttribute("for");
		WebElement element = container.findElement(By.id(id));
		setText(element, text);
	}

	private void setText(WebElement element, String text) {
		if (element.getTagName().equalsIgnoreCase("select")) {
			Select select = new Select(element);
			select.selectByVisibleText(text);
		} else {
			if (!element.getTagName().equalsIgnoreCase("input") && !element.getTagName().equalsIgnoreCase("textarea")) {
				element = element.findElement(By.xpath(".//input"));
			}
			element.clear();
			element.sendKeys(text);
			// TODO replace with 'blur'
			element.sendKeys("\t");
		}
	}
	
	public void waitScript() {
		waitScript((JavascriptExecutor) driver);
	}

	public static void waitScript(JavascriptExecutor driver) {
		do {
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				throw new RuntimeException();
			}
		} while ((Long) driver.executeScript("return pendingRequests") > 0);
	}
}
