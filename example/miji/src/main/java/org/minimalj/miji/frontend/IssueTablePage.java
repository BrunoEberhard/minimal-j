package org.minimalj.miji.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.SimpleTableEditorPage;
import org.minimalj.miji.model.Jira.Issue;
import org.minimalj.security.Subject;

public class IssueTablePage extends SimpleTableEditorPage<Issue> {

	private static final Object[] keys = new Object[] { Issue.$.key, Issue.$.summary };
	
	public IssueTablePage() {
		super(keys);
	}
	
	@Override
	protected List<Issue> load() {
		return Backend.find(Issue.class, new SimpleCriteria(Issue.$.creator, Subject.getCurrent().getName()));
	}

	@Override
	protected Form<Issue> createForm(boolean editable, boolean newObject) {
		Form<Issue> form = new Form<>(editable, 2);
		form.line(Issue.$.key, new IssueFormElement(Issue.$.parent, false));
		form.line(Issue.$.assignee);
		form.line(Issue.$.summary);
		form.line(Issue.$.description);
		return form;
	}
}
