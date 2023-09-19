package org.minimalj.example.timesheet.frontend;

import java.util.List;

import org.minimalj.example.timesheet.model.Project;
import org.minimalj.example.timesheet.model.WorkForProject;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.ListFormElement;
import org.minimalj.frontend.form.element.LookupFormElement;
import org.minimalj.model.Keys;

public class WorkForProjectsFormElement extends ListFormElement<WorkForProject> {

	public WorkForProjectsFormElement(Object key) {
		super(Keys.getProperty(key));
	}

	@Override
	protected void showEntry(WorkForProject entry) {
		add(entry.project.name + ": " + entry.hours.toPlainString());
	}

	@Override
	protected void show(List<WorkForProject> objects) {
		super.show(objects);
		add(new AddListEntryEditor());
	}
	
	@Override
	protected Form<WorkForProject> createForm(boolean edit) {
		Form<WorkForProject> form = new Form<>(edit);
		form.line(new LookupFormElement<>(WorkForProject.$.project, Project.$.name, Project.$.description));
		form.line(WorkForProject.$.hours);
		return form;
	}

}
