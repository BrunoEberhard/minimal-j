package org.minimalj.miji.frontend;

import org.minimalj.frontend.form.element.AbstractObjectFormElement;
import org.minimalj.miji.model.Jira.Issue;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;

public class IssueFormElement extends AbstractObjectFormElement<Issue> {

	public IssueFormElement(PropertyInterface property, boolean editable) {
		super(property, editable);
	}

	public IssueFormElement(Issue key, boolean editable) {
		super(Keys.getProperty(key), editable);
	}

	@Override
	protected void show(Issue issue) {
		add(issue.key, new IssuePage(issue));
	}

}
