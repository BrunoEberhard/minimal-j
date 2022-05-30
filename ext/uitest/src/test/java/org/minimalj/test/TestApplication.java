package org.minimalj.test;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.EmptyPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageAction;
import org.minimalj.security.Authentication;
import org.minimalj.security.Subject;
import org.minimalj.transaction.Role;
import org.minimalj.util.resources.Resources;

public class TestApplication extends Application {
	private final AuthenticatonMode authenticatonMode;

	public static final String TEST_PAGE_TITLE = "Title Test Page";
	
	public TestApplication(AuthenticatonMode authenticatonMode) {
		this.authenticatonMode = authenticatonMode;
	}

	@Override
	public AuthenticatonMode getAuthenticatonMode() {
		return authenticatonMode;
	}

	@Override
	public Authentication createAuthentication() {
		return new TestAuthentication();
	}
	
	@Override
	public Page createDefaultPage() {
		if (Subject.currentHasRole(TestAuthentication.ROLE_TEST)) {
			return new TestPage("TestPage");
		} else {
			return new EmptyPage();
		}
	}

	@Override
	public List<Action> getNavigation() {
		List<Action> actions = new ArrayList<>();
		if (Subject.getCurrent() != null) {
			actions.add(new Action(Resources.getString("ActionWithLogin")) {
				@Override
				public void run() {
				}
			});
		} else {
			actions.add(new Action(Resources.getString("ActionWithoutLogin")) {
				@Override
				public void run() {
				}
			});
		}
		actions.add(new PageAction(new TestPage("Page 2"), "Other Page"));
		return actions;
	}
	
	@Role(TestAuthentication.ROLE_TEST)
	public static class TestPage implements Page {
		private final String text;

		public TestPage(String text) {
			this.text = text;
		}

		@Override
		public String getTitle() {
			return TEST_PAGE_TITLE;
		}

		@Override
		public IContent getContent() {
			String subjectName = Subject.getCurrent() != null ? Subject.getCurrent().getName() : "-";
			return Frontend.getInstance().createHtmlContent("<p>" + text + "</p><p>Subject: " + subjectName + "</p>");
		}
	}
}