package ch.openech.mj.example.persistence;

import static ch.openech.mj.example.model.Book.BOOK;

import java.sql.SQLException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import ch.openech.mj.db.DbPersistence;
import ch.openech.mj.db.SearchableTable;
import ch.openech.mj.db.model.ColumnAccess;
import ch.openech.mj.example.model.Book;

public class BookTable extends SearchableTable<Book> {

	private static final String[] INDEX_FIELDS = {
		BOOK.title, //
		BOOK.author, //
		BOOK.date, //
	};
	
	public BookTable(DbPersistence dbPersistence) throws SQLException {
		super(dbPersistence, Book.class, INDEX_FIELDS);
	}

	@Override
	protected Field getField(String fieldName, Book object) {
		Field.Index index = Field.Index.ANALYZED;
		if (fieldName.toLowerCase().contains("date")) {
			index = Field.Index.NOT_ANALYZED;
		}
		
		String value = (String) ColumnAccess.getValue(object, fieldName);
		if (value != null) {
			return new Field(fieldName, value, Field.Store.YES, index);
		} else {
			return null;
		}
	}

	@Override
	protected Book documentToObject(Document document) {
		int id = Integer.parseInt(document.get("id"));
		try {
			return read(id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected Book createResultObject() {
		return new Book();
	}

	@Override
	protected void setField(Book result, String fieldName, String value) {
		ColumnAccess.setValue(result, fieldName, value);
	}

}
