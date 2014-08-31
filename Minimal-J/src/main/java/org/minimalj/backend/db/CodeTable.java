package org.minimalj.backend.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.minimalj.util.CodeUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;

/**
 * Minimal-J internal<p>
 * 
 * Rows in a immutable table are created and reused. But
 * they are never changed nor deleted.
 * 
 * Code tables don't need an id. The unique field in
 * the code class is used instead of a generated id.
 * 
 */
public class CodeTable<T> extends Table<T> {
	
	protected final String selectByCodeQuery;
	
	public CodeTable(DbPersistence dbPersistence, Class<T> clazz) {
		super(dbPersistence, clazz);
		if (!CodeUtils.isCode(clazz)) {
			throw new IllegalArgumentException(clazz.getName());
		}
		this.selectByCodeQuery = selectByCodeQuery();
	}
	
	@Override
	protected void addSpecialColumns(DbSyntax syntax, StringBuilder s) {
		// for codes an INT column as ID is enough
		syntax.addIdColumn(s, false);
	}
	
	@Override
	public void createTable(DbSyntax syntax) {
		super.createTable(syntax);
		execute(syntax.createUniqueIndex(getTableName(), StringUtils.toDbName(CodeUtils.getCodeProperty(clazz).getFieldName())));
	}

	public void update(T object) {
		Object code = CodeUtils.getCode(object);
		Object existingObject = readByCode(code);
		long id = IdUtils.getId(existingObject);
		super.update(id, object);
	}
	
	public T readByCode(Object code) {
		try {
			PreparedStatement selectByCodeStatement = getStatement(dbPersistence.getConnection(), selectByCodeQuery, false);
			selectByCodeStatement.setObject(1, code);
			T object = executeSelect(selectByCodeStatement);
			return object;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read " + getTableName() + " with code " + code);
		}
	}

	protected String selectByCodeQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); 
		query.append(" WHERE ");
		query.append(StringUtils.toDbName(CodeUtils.getCodeProperty(clazz).getFieldName()));
		query.append(" = ?");
		return query.toString();
	}

}
