package org.minimalj.example.notes;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.transaction.criteria.Criteria;

public class NoteTablePage extends TablePage<Note> {

	private static final Object[] columns = new Object[]{Note.$.date, Note.$.text};
	
	public NoteTablePage() {
		super(columns);
	}

	@Override
	protected List<Note> load() {
		return Backend.persistence().read(Note.class, Criteria.all(), Integer.MAX_VALUE);
	}
}
