package org.minimalj.example.notes;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;

public class NewNoteEditor extends SimpleEditor<Note> {

	@Override
	protected Form<Note> createForm() {
		return new NoteForm();
	}

	@Override
	protected Note save(Note object) {
		return Backend.getInstance().insert(object);
	}

	private static class NoteForm extends Form<Note> {
		
		public NoteForm() {
			line(Note.$.date);
			line(Note.$.text);
		}
	}
}
