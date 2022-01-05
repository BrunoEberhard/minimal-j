package org.minimalj.test;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.Page;
import org.minimalj.security.Authentication;
import org.minimalj.security.Subject;
import org.minimalj.util.resources.Resources;

public class TestApplication extends Application {
	private final AuthenticatonMode authenticatonMode;

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
		return new TestPage("TestPage");
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
		return actions;
	}
	
	public static class TestPage extends Page {
		private final String text;

		public TestPage(String text) {
			this.text = text;
		}

		@Override
		public String getTitle() {
			return "Title Test Page";
		}

		@Override
		public IContent getContent() {
			String subjectName = Subject.getCurrent() != null ? Subject.getCurrent().getName() : "-";
			return Frontend.getInstance().createHtmlContent("<p>" + text + "</p><p>Subject: " + subjectName + "</p>");
		}
	}
}