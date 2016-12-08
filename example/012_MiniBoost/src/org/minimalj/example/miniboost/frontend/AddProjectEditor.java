package org.minimalj.example.miniboost.frontend;

import org.minimalj.backend.Backend;
import org.minimalj.example.miniboost.model.Project;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;

public class AddProjectEditor extends NewObjectEditor<Project> {

	@Override
	protected Form<Project> createForm() {
		return new ProjectForm(Form.EDITABLE);
	}

	@Override
	protected Project save(Project owner) {
		return Backend.save(owner);
	}
	
	@Override
	protected void finished(Project newProject) {
		Frontend.show(new ProjectTablePage());
	}

}
