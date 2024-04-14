package org.minimalj.example.timesheet.frontend;

import org.minimalj.backend.Backend;
import org.minimalj.example.timesheet.model.Work;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;

public class NewWorkEditor extends NewObjectEditor<Work> {

	@Override
	protected Form<Work> createForm() {
		Form<Work> form = new Form<>();
		form.line(Work.$.date);
		form.line(new WorkForProjectsFormElement(Work.$.workForProjects));
		return form;
	}

	@Override
	protected Work save(Work work) {
		return Backend.save(work);
	}
	
}
