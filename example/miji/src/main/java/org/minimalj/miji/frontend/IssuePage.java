package org.minimalj.miji.frontend;

import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.miji.model.Jira.Issue;

public class IssuePage extends ObjectPage<Issue> {

	public IssuePage(Issue issue) {
		super(issue);
	}

	@Override
	protected Form<Issue> createForm() {
		Form<Issue> form = new Form<>(false, 2);
		form.line(Issue.$.key, new IssueFormElement(Issue.$.parent, false));
		form.line(Issue.$.assignee);
		form.line(Issue.$.summary);
		form.line(Issue.$.description);
		return form;
	}

}
