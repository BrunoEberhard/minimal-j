package org.minimalj.miji.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.TablePage.SimpleTablePageWithDetail;
import org.minimalj.miji.model.Jira.Issue;
import org.minimalj.security.Subject;

public class IssueTablePage extends SimpleTablePageWithDetail<Issue> {

	private static final Object[] keys = new Object[] { Issue.$.key, Issue.$.summary };
	
	public IssueTablePage() {
		super(keys);
	}
	
	@Override
	protected List<Issue> load() {
		return Backend.find(Issue.class, new SimpleCriteria(Issue.$.assignee, Subject.getCurrent().getName()));
	}

	@Override
	protected ObjectPage<Issue> createDetailPage(Issue issue) {
		return new IssuePage(issue);
	}
}
