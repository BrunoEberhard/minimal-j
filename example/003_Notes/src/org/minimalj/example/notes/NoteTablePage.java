package org.minimalj.example.notes;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.repository.query.By;

public class NoteTablePage extends TablePage<Note> {
	
	@Override
	protected Object[] getColumns() {
		return new Object[] { Note.$.date, Note.$.text };
	}

	@Override
	protected List<Note> load() {
		return Backend.find(Note.class, By.all());
	}
}
