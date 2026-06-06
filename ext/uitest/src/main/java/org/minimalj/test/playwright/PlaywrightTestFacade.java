package org.minimalj.test.playwright;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.impl.web.WebServer;
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
import org.minimalj.test.UiTest.UiTestBrowser;
import org.minimalj.test.UiTestFacade;
import org.minimalj.test.web.WebTestUtil;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.ElementState;
import com.microsoft.playwright.options.MouseButton;
import com.microsoft.playwright.options.SelectOption;

/**
 * UI-test facade based on <a href="https://playwright.dev/java/">Playwright</a>.
 * <p>
 * It implements the exact same
 * {@link UiTestFacade} and works the {@link WebServer} rendered
 * JsonFrontend html.
 */
public class PlaywrightTestFacade implements UiTestFacade {
	private static final Logger logger = Logger.getLogger(PlaywrightTestFacade.class.getName());

	private final UiTestBrowser driverName;
	private final boolean headless;

	private Playwright playwright;
	private Browser browser;
	private BrowserContext context;
	private Page page;

	public PlaywrightTestFacade(UiTestBrowser driverName, boolean headless) {
		this.driverName = driverName;
		this.headless = headless;
	}

	private void init() {
		if (page == null) {
			String language = Locale.getDefault().getLanguage();
			logger.info("Language for uitest is: " + language);

			playwright = Playwright.create();
			BrowserType browserType;
			switch (driverName) {
			case chrome:
				browserType = playwright.chromium();
				break;
			case firefox:
				browserType = playwright.firefox();
				break;
			default:
				throw new IllegalStateException();
			}

			browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(headless));
			context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1600, 900).setLocale(language));
			page = context.newPage();

			Runtime.getRuntime().addShutdownHook(new Thread(this::closePlaywright));
		}
	}

	private void closePlaywright() {
		try {
			if (playwright != null) {
				playwright.close();
			}
		} catch (Exception e) {
			// ignore on shutdown
		}
	}

	@Override
	public void start(Application application) {
		WebServer.start(application);
		init();
		reload();
	}

	public void reload() {
		page.navigate("about:blank");
		String portString = Configuration.get("MjFrontendPort", "8080");
		page.navigate("http://localhost:" + portString);
		waitScript();
	}

	public void shutdown() {
		closePlaywright();
		playwright = null;
		browser = null;
		context = null;
		page = null;
	}

	@Override
	public UserPasswordLoginTestFacade getLoginTestFacade() {
		Assertions.assertEquals(Resources.getString("Login.title"), page.title());

		return new HtmlLoginTestFacade();
	}

	public class HtmlLoginTestFacade implements UserPasswordLoginTestFacade {

		@Override
		public boolean hasSkipLogin() {
			return findByXpath("//button[text()=" + escapeXpath(Resources.getString("SkipLoginAction")) + "]") != null;
		}

		@Override
		public boolean hasClose() {
			return false;
		}

		@Override
		public void login() {
			clickButton("LoginAction");
		}

		@Override
		public void cancel() {
			clickButton("LoginAction");
		}

		@Override
		public void close() {
		}

		@Override
		public void setUser(String name) {
			setText(page.querySelector("#pageContainer"), Resources.getString("UserPassword.user"), name);
		}

		@Override
		public void setPassword(String password) {
			setText(page.querySelector("#pageContainer"), Resources.getString("UserPassword.password"), password);
		}
	}

	@Override
	public void logout() {
		ElementHandle element = page.querySelector("#logout");
		executeOnClick(element);
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
			ElementHandle divPageContainer = page.querySelector("#pageContainer");
			List<PageTestFacade> pages = new ArrayList<>();
			for (ElementHandle divPage : divPageContainer.querySelectorAll(".page")) {
				pages.add(new HtmlPageTestFacade(divPage));
			}
			return pages;
		}

		@Override
		public DialogTestFacade getDialog() {
			List<ElementHandle> dialogs = page.querySelectorAll("dialog");
			if (!dialogs.isEmpty()) {
				return new HtmlDialogTestFacade(dialogs.get(dialogs.size() - 1));
			} else {
				List<ElementHandle> contentLogins = page.querySelectorAll(".contentLogin");
				if (!contentLogins.isEmpty()) {
					return new HtmlDialogTestFacade(contentLogins.get(0));
				}
			}
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

		@Override
		public boolean hasLogout() {
			ElementHandle logout = page.querySelector("#logout");
			return logout != null && logout.isVisible();
		}

		@Override
		public void logout() {
			ElementHandle logout = page.querySelector("#logout");
			executeOnClick(logout);
			waitScript();
		}

		private class HtmlNavigationTestFacade implements NavigationTestFacade {

			@Override
			public Runnable get(String text) {
				ElementHandle divNavigation = page.querySelector("#navigation");
				ElementHandle item = divNavigation.querySelector("xpath=.//a[text()=" + escapeXpath(text) + "]");
				if (item == null) {
					return null;
				}
				return () -> {
					if (!divNavigation.isVisible()) {
						ElementHandle navigationToggle = page.querySelector("#navigationToggle");
						navigationToggle.click();
						item.waitForElementState(ElementState.VISIBLE);
					}
					item.click();
					waitScript();
				};
			}
		}

		private class BackTestFacade implements ActionTestFacade {
			@Override
			public void run() {
				page.goBack();
				forwards++;
			}

			@Override
			public boolean isEnabled() {
				return (Boolean) page.evaluate("() => window.history.length > 0");
			}
		}

		private class FowardTestFacade implements ActionTestFacade {
			@Override
			public void run() {
				page.goForward();
				forwards--;
			}

			@Override
			public boolean isEnabled() {
				return forwards > 0;
			}
		}
	}

	private class HtmlPageTestFacade implements PageTestFacade {
		private final ElementHandle divPage;

		public HtmlPageTestFacade(ElementHandle divPage) {
			this.divPage = divPage;
		}

		@Override
		public void executeQuery(String query) {
			ElementHandle inputQuerySearch = divPage.querySelector("#querySearch");
			if (inputQuerySearch == null) {
				throw new IllegalStateException("Page is not a query page");
			}
			inputQuerySearch.click();
			page.keyboard().type(query);
			page.keyboard().press("Enter");
			waitScript();
		}

		@Override
		public String getTitle() {
			ElementHandle spanPageTitle = divPage.querySelector(".pageTitle");
			// textContent works also if the title is not visible (which it is not on small screens).
			// innerText() would return an empty string for a hidden element.
			return spanPageTitle.textContent();
		}

		@Override
		public TableTestFacade getTable() {
			ElementHandle table = divPage.querySelector(".table");
			if (table == null) {
				throw new IllegalStateException("Page is not a table page");
			}
			return new HtmlTableTestFacade(divPage, table);
		}

		@Override
		public FormTestFacade getForm() {
			ElementHandle form = divPage.querySelector(".form");
			if (form == null || !classOf(form.querySelector("xpath=..")).contains("pageContent")) {
				throw new IllegalArgumentException(
						"Page is a table not a form. If you want access the filter of the table please use getTable().getOverview() or table().getOverview()");
			}
			return new HtmlFormTestFacade(form);
		}

		@Override
		public boolean contains(String string) {
			ElementHandle iframe = divPage.querySelector("iframe");
			Frame frame = iframe.contentFrame();
			ElementHandle body = frame.querySelector("body");
			return body.innerText().contains(string);
		}

		@Override
		public NavigationTestFacade getContextMenu() {
			return new PageContextMenuTestFacade(divPage);
		}
	}

	private class PageContextMenuTestFacade implements NavigationTestFacade {
		private final ElementHandle divPage;

		public PageContextMenuTestFacade(ElementHandle divPage) {
			this.divPage = divPage;
		}

		@Override
		public Runnable get(String text) {
			return () -> {
				divPage.click(new ElementHandle.ClickOptions().setButton(MouseButton.RIGHT));

				ElementHandle actionMenu = divPage.querySelector(".contextMenu");
				ElementHandle item;
				if (actionMenu != null && actionMenu.isVisible()) {
					item = actionMenu.querySelector("xpath=.//*[text()=" + escapeXpath(text) + "]");
				} else {
					ElementHandle actionMenuButton = divPage.querySelector(".actionMenuButton");
					if (actionMenuButton == null || !actionMenuButton.isVisible()) {
						actionMenuButton = page.querySelector("#actionMenuButton");
					}
					actionMenuButton.click();
					item = divPage.querySelector("xpath=.//*[text()=" + escapeXpath(text) + "]");
				}
				Assertions.assertFalse("true".equals(item.getAttribute("disabled")), "Context action should not be disabled: " + text);
				item.click();
				waitScript();
			};
		}
	}

	private class HtmlDialogTestFacade implements DialogTestFacade {
		private final ElementHandle dialog;

		public HtmlDialogTestFacade(ElementHandle dialog) {
			this.dialog = dialog;
		}

		@Override
		public String getTitle() {
			return dialog.querySelector(".dialogHeader span").innerText();
		}

		@Override
		public void close() {
			getAction(Resources.getString("CancelAction")).run();
		}

		@Override
		public FormTestFacade getForm() {
			ElementHandle form = dialog.querySelector(".form");
			return new HtmlFormTestFacade(form);
		}

		@Override
		public TableTestFacade getTable() {
			ElementHandle table = dialog.querySelector(".table");
			if (table == null) {
				throw new IllegalStateException("Dialog has no table");
			}
			return new HtmlTableTestFacade(null, table);
		}

		@Override
		public SearchTableTestFacade getSearchTable() {
			return new HtmlSearchTableTestFacade(dialog);
		}

		@Override
		public ActionTestFacade getAction(String caption) {
			ElementHandle button = dialog.querySelector("xpath=.//button[text()=" + escapeXpath(caption) + "]");
			return new HtmlActionTestFacade(button);
		}

		@Override
		public FormTestFacade formWithElement(String caption) {
			ElementHandle element = dialog.querySelector("xpath=.//label[text()=" + escapeXpath(caption) + "]");
			if (element == null) {
				element = dialog.querySelector("xpath=.//*[@name=" + escapeXpath(caption) + "]");
			}
			if (element == null) {
				return null;
			}
			// closest() walks up from the element to the nearest enclosing form
			ElementHandle form = element.evaluateHandle("e => e.closest('.form')").asElement();
			return form != null ? new HtmlFormTestFacade(form) : null;
		}

		@Override
		public FormTestFacade nextForm(FormTestFacade form) {
			if (!(form instanceof HtmlFormTestFacade)) {
				return null;
			}
			ElementHandle formElement = ((HtmlFormTestFacade) form).form;
			ElementHandle nextForm = formElement.evaluateHandle("f => {" //
					+ "  var row = f.closest('.formElement').parentElement;" //
					+ "  var css = row.className;" //
					+ "  if (css.includes('groupEnd') || css.includes('groupSingleRow')) return null;" //
					+ "  var next = row.nextElementSibling;" //
					+ "  return next ? next.querySelector('.form') : null;" //
					+ "}").asElement();
			return nextForm != null ? new HtmlFormTestFacade(nextForm) : null;
		}
	}

	private class HtmlActionTestFacade implements ActionTestFacade {
		private final ElementHandle button;

		public HtmlActionTestFacade(ElementHandle button) {
			this.button = button;
		}

		@Override
		public void run() {
			button.evaluate("e => e.click()");
			waitScript();
		}

		@Override
		public boolean isEnabled() {
			return button.getAttribute("disabled") == null;
		}
	}

	private class HtmlFormTestFacade implements FormTestFacade {
		private final ElementHandle form;

		public HtmlFormTestFacade(ElementHandle form) {
			this.form = form;
		}

		@Override
		public void printElementCaptions() {
			List<ElementHandle> labels = form.querySelectorAll("label");
			List<String> captions = labels.stream()
					.filter(ElementHandle::isVisible)
					.map(ElementHandle::innerText)
					.filter(text -> !text.isEmpty())
					.collect(Collectors.toList());
			System.out.println("Captions of visible elements: " + String.join(", ", captions));
		}

		@Override
		public FormElementTestFacade getElement(String caption, Boolean isBooleanValue) {
			List<ElementHandle> labels = form.querySelectorAll("xpath=.//label[text()=" + escapeXpath(caption) + "]");
			for (var label : labels) {
				String id = label.getAttribute("for");
				if (id != null) {
					ElementHandle element = byId(form, id);
					String type = element.getAttribute("type");
					boolean isRadioOrCheckBox = StringUtils.equals(type, "radio", "checkbox");
					if (isBooleanValue == null || isRadioOrCheckBox == isBooleanValue) {
						return new HtmlFormElementTestFacade(element);
					}
				} else if (!Boolean.FALSE.equals(isBooleanValue)) {
					if (StringUtils.equals(label.getAttribute("class"), "checkboxLabel")) {
						return new HtmlFormElementTestFacade(label.querySelector("input"));
					}
				}
			}
			List<ElementHandle> elementsByName = form.querySelectorAll("xpath=.//*[@name=" + escapeXpath(caption) + "]");
			if (!elementsByName.isEmpty()) {
				return new HtmlFormElementTestFacade(elementsByName.get(0));
			}
			return null;
		}

		@Override
		public FormElementTestFacade getElement(String caption, int index) {
			ElementHandle label = form.querySelectorAll("xpath=.//label[text()=" + escapeXpath(caption) + "]").get(index);
			String id = label.getAttribute("for");
			ElementHandle element = byId(form, id);
			return new HtmlFormElementTestFacade(element);
		}

		@Override
		public FormElementTestFacade getElement(int row, int column) {
			ElementHandle rowElement = form.querySelectorAll(":scope > div").get(row);
			ElementHandle element = rowElement.querySelectorAll(":scope > div").get(column);
			return new HtmlFormElementTestFacade(element);
		}

		@Override
		public ActionTestFacade getAction(String label) {
			ElementHandle rowElement = form.querySelector("xpath=.//a[text()=" + escapeXpath(label) + "]");
			return new HtmlActionTestFacade(rowElement);
		}
	}

	private class HtmlFormElementTestFacade implements FormElementTestFacade {
		private final ElementHandle formElement;

		public HtmlFormElementTestFacade(ElementHandle formElement) {
			this.formElement = formElement;
		}

		@Override
		public String getText() {
			return PlaywrightTestFacade.this.getText(formElement);
		}

		@Override
		public void setText(String value) {
			try {
				PlaywrightTestFacade.this.setText(formElement, value);
				waitScript();
			} catch (PlaywrightException e) {
				// catch this exception, value simply stays the same
			}
		}

		@Override
		public boolean isChecked() {
			ElementHandle element = formElement;
			if (!tagName(element).equals("input")) {
				element = element.querySelector("xpath=.//input");
			}
			return element.isChecked();
		}

		@Override
		public void setChecked(boolean checked) {
			ElementHandle element = formElement;
			if (!tagName(element).equals("input")) {
				element = element.querySelector("xpath=.//input");
			}
			if (element.isChecked() != checked) {
				element.click();
			}
			waitScript();
		}

		@Override
		public List<String> getComboBoxValues() {
			List<ElementHandle> options = formElement.querySelectorAll("option");
			List<String> texts = new ArrayList<>(options.size());
			for (ElementHandle option : options) {
				texts.add(option.innerText());
			}
			return texts;
		}

		@Override
		public String getValidation() {
			String id = formElement.getAttribute("id");
			ElementHandle formElement = page.querySelector("xpath=//*[@id=" + escapeXpath(id) + "]");
			String validation = formElement.getAttribute("title");
			return StringUtils.isEmpty(validation) ? null : validation;
		}

		@Override
		public void lookup() {
			ElementHandle lookupButton = formElement.querySelector(".lookupButton");
			executeOnClick(lookupButton);
			waitScript();
		}

		@Override
		public void action(String text) {
			ElementHandle dropdownButton = formElement.querySelector("div.dropdownButton");
			dropdownButton.click();
			waitScript();
			ElementHandle actionMenu = formElement.querySelector("div.dropdown");
			ElementHandle item = actionMenu.querySelector("xpath=.//*[text()=" + escapeXpath(text) + "]");
			item.click();
			waitScript();
		}

		@Override
		public String getLine(int line) {
			ElementHandle divGroupVertical = formElement.querySelector(".groupVertical");
			ElementHandle divGroupItem = divGroupVertical.querySelectorAll(".groupItem").get(line);
			ElementHandle divText = divGroupItem.querySelector(".text");
			return divText.innerText();
		}

		@Override
		public List<ActionTestFacade> getLineActions(int line) {
			ElementHandle divGroupVertical = formElement.querySelector(".groupVertical");
			ElementHandle divGroupItem = divGroupVertical.querySelectorAll(".groupItem").get(line);
			ElementHandle divDropdown = divGroupItem.querySelector(".dropdown");
			List<ElementHandle> divActions = divDropdown.querySelectorAll("div");
			return divActions.stream().map(this::lineAction).collect(Collectors.toList());
		}

		private ActionTestFacade lineAction(ElementHandle divAction) {
			return new ActionTestFacade() {
				@Override
				public void run() {
					executeOnClick(divAction);
					waitScript();
				}

				@Override
				public boolean isEnabled() {
					return true;
				}

				@Override
				public String toString() {
					return divAction.innerText();
				}

				@Override
				public String getDescription() {
					return divAction.getAttribute("title");
				}
			};
		}

		@Override
		public FormElementTestFacade groupItem(int... positions) {
			ElementHandle element = formElement;
			while (!classOf(element).contains("formElement")) {
				element = element.querySelector("xpath=..");
			}
			for (int pos : positions) {
				element = element.querySelectorAll(":scope > div > div").get(pos);
			}
			return new HtmlFormElementTestFacade(element);
		}

		@Override
		public FormTestFacade row(int pos) {
			ElementHandle groupVertical = formElement.querySelector("xpath=.//div[@class='groupVertical']");
			ElementHandle groupItemElement = groupVertical.querySelectorAll(":scope > div").get(pos).querySelector(":scope > div");
			return new HtmlFormTestFacade(groupItemElement);
		}
	}

	private class HtmlTableTestFacade implements TableTestFacade {
		protected final ElementHandle page;
		protected final ElementHandle table;

		public HtmlTableTestFacade(ElementHandle page, ElementHandle table) {
			this.page = page;
			this.table = table;
		}

		@Override
		public int getColumnCount() {
			ElementHandle thead = table.querySelector("thead");
			return thead.querySelectorAll("th.col").size();
		}

		@Override
		public int getRowCount() {
			ElementHandle tbody = table.querySelector("tbody");
			return tbody.querySelectorAll("tr").size();
		}

		@Override
		public String getHeader(int column) {
			ElementHandle thead = table.querySelector("thead");
			return thead.querySelectorAll("tr.headers th").get(column).innerText();
		}

		@Override
		public String getValue(int row, int column) {
			ElementHandle tbody = table.querySelector("tbody");
			ElementHandle tr = tbody.querySelectorAll("tr").get(row);
			return tr.querySelectorAll("td").get(column).innerText();
		}

		@Override
		public void activate(int row, int column) {
			ElementHandle tbody = table.querySelector("tbody");
			ElementHandle tr = tbody.querySelectorAll("tr").get(row);
			ElementHandle td = tr.querySelectorAll("td").get(column);
			td.click();
			waitScript();
		}

		@Override
		public void activate(int row) {
			ElementHandle tbody = table.querySelector("tbody");
			ElementHandle tr = tbody.querySelectorAll("tr").get(row);
			ElementHandle firstTdWithoutLink = tr.querySelector("xpath=td[not(child::a)][1]");
			firstTdWithoutLink.dblclick();
			waitScript();
		}

		@Override
		public void select(int row) {
			ElementHandle tbody = table.querySelector("tbody");
			ElementHandle tr = tbody.querySelectorAll("tr").get(row);
			List<ElementHandle> cells = tr.querySelectorAll("td");

			// Find first cell without a link. We don't want the link to be activated but the row to be selected.
			for (ElementHandle cell : cells) {
				if (cell.getAttribute("onclick") == null) {
					cell.click();
					waitScript();
					return;
				}
			}

			throw new IllegalArgumentException("Cannot select row as there is no cell without link");
		}

		@Override
		public FormTestFacade getOverview() {
			ElementHandle form = table.querySelector("xpath=..").querySelector(".form");
			return new HtmlFormTestFacade(form);
		}

		@Override
		public boolean isFilterVisible() {
			ElementHandle filter = table.querySelector(".columnFilters");
			return filter.isVisible();
		}

		@Override
		public void setFilterVisible(boolean visible) {
			if (visible != isFilterVisible()) {
				ElementHandle filterButton = page.querySelector(".filterButton");
				if (filterButton == null || !filterButton.isVisible()) {
					filterButton = PlaywrightTestFacade.this.page.querySelector("#tableFilterButton");
				}
				filterButton.click();
				waitScript();
			}
		}

		@Override
		public void setFilter(int column, String filterString) {
			ElementHandle columnFilter = getColumnFilter(column);
			PlaywrightTestFacade.this.setText(columnFilter, filterString);
			waitScript();
		}

		@Override
		public String getFilter(int column) {
			ElementHandle columnFilter = getColumnFilter(column);
			return getText(columnFilter);
		}

		private ElementHandle getColumnFilter(int column) {
			ElementHandle columnFilters = table.querySelector(".columnFilters");
			return columnFilters.querySelectorAll("th").get(column);
		}

		@Override
		public void filterLookup(int column) {
			ElementHandle columnFilter = getColumnFilter(column);
			ElementHandle lookupButton = columnFilter.querySelector(".lookupButton");
			executeOnClick(lookupButton);
			waitScript();
		}
	}

	private class HtmlSearchTableTestFacade extends HtmlTableTestFacade implements SearchTableTestFacade {

		public HtmlSearchTableTestFacade(ElementHandle dialog) {
			super(null, dialog);
		}

		@Override
		public void search(String text) {
			ElementHandle input = table.querySelector("input");
			PlaywrightTestFacade.this.setText(input, text);
			ElementHandle button = table.querySelector("button");
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
		ElementHandle element = findByXpath("//button[text()=" + escapeXpath(caption) + "]");
		executeOnClick(element);
		waitScript();
	}

	private void setText(ElementHandle container, String caption, String text) {
		ElementHandle label = container.querySelector("xpath=.//label[text()=" + escapeXpath(caption) + "]");
		String id = label.getAttribute("for");
		ElementHandle element = byId(container, id);
		setText(element, text);
	}

	private ElementHandle findValueElement(ElementHandle element) {
		var tagName = tagName(element);
		while (!tagName.equals("input") && !tagName.equals("textarea") && !tagName.equals("select")) {
			ElementHandle child = element.querySelector("input,textarea,select,div");
			if (child == null) {
				break;
			}
			element = child;
			tagName = tagName(element);
		}
		return element;
	}

	private void setText(ElementHandle element, String text) {
		element = findValueElement(element);
		if (tagName(element).equals("select")) {
			// Playwright auto-waits up to its default timeout (30s) if the requested option
			// is not present. Selenium's selectByVisibleText fails immediately, so check the
			// available options first and throw at once (caught by HtmlFormElementTestFacade.setText).
			boolean optionExists = element.querySelectorAll("option").stream()
					.map(option -> option.textContent() != null ? option.textContent().trim() : "")
					.anyMatch(label -> StringUtils.equals(label, text));
			if (!optionExists) {
				throw new PlaywrightException("No option with label '" + text + "'");
			}
			element.selectOption(new SelectOption().setLabel(text));
		} else {
			element.click();
			element.press("Control+a");
			if (!StringUtils.isEmpty(text)) {
				// the browser makes a call to the server when one character is entered (to refresh validation)
				// we must wait for the response because the response could otherwise overwrite further characters
				page.keyboard().type(text.substring(0, 1));
				if (text.length() > 1) {
					waitScript();
					page.keyboard().type(text.substring(1));
				}
			} else {
				element.fill("");
				// same reason for waitScript as with the first character
				waitScript();
			}
			// blur
			page.keyboard().press("Tab");
		}
	}

	private String getText(ElementHandle element) {
		String tagName = tagName(element);
		if (tagName.equals("a") || tagName.equals("div") && classOf(element).contains("text")) {
			return element.innerText();
		}
		element = findValueElement(element);
		tagName = tagName(element);
		if (tagName.equals("select")) {
			Object selected = element.evaluate("e => e.selectedIndex >= 0 ? e.options[e.selectedIndex].textContent : null");
			return selected != null ? selected.toString() : null;
		} else if (tagName.equals("div")) {
			List<ElementHandle> anchors = element.querySelectorAll("a");
			if (anchors.size() == 1) {
				return anchors.get(0).innerText();
			}
			return element.innerText();
		} else {
			return element.inputValue();
		}
	}

	public void waitScript() {
		while (Boolean.TRUE.equals(page.evaluate("() => pendingRequests > 0"))) {
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				throw new RuntimeException();
			}
		}
	}

	//

	private ElementHandle findByXpath(String xpath) {
		return page.querySelector("xpath=" + xpath);
	}

	/**
	 * Execute the javascript declared in the {@code onclick} attribute of an
	 * element. Minimal-J renders the actual call (e.g. {@code action("uuid")}) into
	 * the onclick attribute; executing it directly is more robust than a real click
	 * because it is independent of the element being scrolled into the viewport.
	 */
	private void executeOnClick(ElementHandle element) {
		String onclick = element.getAttribute("onclick");
		if (!StringUtils.isEmpty(onclick)) {
			page.evaluate("() => { " + onclick + " }");
		} else {
			element.evaluate("e => e.click()");
		}
	}

	private String tagName(ElementHandle element) {
		return ((String) element.evaluate("e => e.tagName")).toLowerCase();
	}

	private String classOf(ElementHandle element) {
		String clazz = element != null ? element.getAttribute("class") : null;
		return clazz != null ? clazz : "";
	}

	private static String escapeXpath(String input) {
		return WebTestUtil.escapeXpath(input);
	}

	/**
	 * Lookup an element by its html id within the given root. Minimal-J uses random
	 * uuids as ids; a leading digit would break a css {@code #id} selector, so an
	 * xpath id-lookup is used instead.
	 */
	private static ElementHandle byId(ElementHandle root, String id) {
		return root.querySelector("xpath=.//*[@id=" + escapeXpath(id) + "]");
	}
}
