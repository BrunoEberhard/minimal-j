package org.minimalj.example.timesheet.frontend;
import org.minimalj.backend.Backend;
import org.minimalj.example.timesheet.model.Project;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;

public class NewProjectEditor extends NewObjectEditor<Project> {

	@Override
	protected Form<Project> createForm() {
		Form<Project> form = new Form<>();
		form.line(Project.$.name);
		form.line(Project.$.description);
		return form;
	}

	@Override
	protected Project save(Project project) {
		return Backend.save(project);
	}
}
