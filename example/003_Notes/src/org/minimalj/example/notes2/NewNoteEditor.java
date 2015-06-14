package org.minimalj.example.notes2;

import org.minimalj.backend.Backend;
import org.minimalj.example.notes.Note;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.toolkit.ClientToolkit;

public class NewNoteEditor extends SimpleEditor<Note> {

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
