package ch.openech.mj.example.persistence;

import static ch.openech.mj.example.model.Customer.CUSTOMER;

import java.sql.SQLException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import ch.openech.mj.db.DbPersistence;
import ch.openech.mj.db.SearchableTable;
import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.example.model.Customer;

public class CustomerTable extends SearchableTable<Customer> {

	private static final Object[] INDEX_FIELDS = {
		CUSTOMER.firstName, //
		CUSTOMER.name, //
	};

	public CustomerTable(DbPersistence dbPersistence) throws SQLException {
		super(dbPersistence, Customer.class, INDEX_FIELDS);
	}

	@Override
	protected Field getField(PropertyInterface property, Customer object) {
		String fieldName = property.getFieldName();
		
		String value = (String) property.getValue(object);
		if (value != null) {
			return new Field(fieldName, value, Field.Store.YES, Field.Index.ANALYZED);
		} else {
			return null;
		}
	}

	@Override
	protected Customer documentToObject(Document document) {
		int id = Integer.parseInt(document.get("id"));
		try {
			return read(id);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected Customer createResultObject() {
		return new Customer();
	}

}
