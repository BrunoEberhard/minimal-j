package org.minimalj.example.notes;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.toolkit.ClientToolkit;

public class NewNoteEditor extends NewObjectEditor<Note> {

	@Override
	protected Form<Note> createForm() {
		return new NoteForm();
	}

	@Override
	protected Note save(Note object) {
		return Backend.getInstance().insert(object);
	}
	
	@Override
	protected void finished(Note result) {
		ClientToolkit.getToolkit().show(new NoteTablePage());
	}

	private static class NoteForm extends Form<Note> {
		
		public NoteForm() {
			line(Note.$.date);
			line(Note.$.text);
		}
	}
}
