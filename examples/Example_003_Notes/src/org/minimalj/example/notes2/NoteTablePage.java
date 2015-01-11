package org.minimalj.example.notes2;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.transaction.criteria.Criteria;

public class NoteTablePage extends TablePage<Note> {

	private static final Object[] columns = new Object[]{Note.$.date, Note.$.text};
	
	public NoteTablePage() {
		super(columns, null);
	}

	@Override
	protected List<Note> load(String query) {
		return Backend.getInstance().read(Note.class, Criteria.all(), Integer.MAX_VALUE);
	}

	@Override
	protected void clicked(Note selectedObject, List<Note> selectedObjects) {
		// ignored, could open the note for editing
	}

}
