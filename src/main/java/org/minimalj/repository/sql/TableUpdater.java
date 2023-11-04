package org.minimalj.repository.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.minimalj.model.EnumUtils;
import org.minimalj.repository.sql.SqlDialect.PostgresqlDialect;

public class TableUpdater {
	private static final Logger logger = Logger.getLogger(TableUpdater.class.getName());
	
	private final SqlRepository repository;
	private final SqlDialect sqlDialect;
	private final SqlIdentifier sqlIdentifier;

	public TableUpdater(SqlRepository repository) {
		this.repository = repository;
		if (!(repository.sqlDialect instanceof PostgresqlDialect)) {
			throw new IllegalArgumentException(repository.sqlDialect + " not supported");
		}
		this.sqlDialect = repository.sqlDialect;
		this.sqlIdentifier = repository.sqlIdentifier;
	}
	
	public void verifySchema(boolean update) {
		updateEnums(update);
		updateTables();
	}
		
	protected void updateTables() {
		List<AbstractTable<?>> createdTables = new ArrayList<>();
		for (AbstractTable<?> table : repository.tables.values()) {
			int count = repository.find(Integer.class, tableExists(table.clazz, table.name), 1).get(0);
			if (count == 0) {
				logger.info("New table: " + table.name);
				table.createTable(sqlDialect);
				createdTables.add(table);
			} else {
				updateTableColumns(table);
			}
		}
		for (AbstractTable<?> table : createdTables) {
			table.createIndexes(sqlDialect);
		}
		for (AbstractTable<?> table : createdTables) {
			table.createConstraints(sqlDialect);
		}
	}

	protected void updateTableColumns(AbstractTable<?> table) {
		// TODO
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void updateEnums(boolean update) {
		for (Class enumClass : repository.enums) {
			String enumIdentifier = sqlIdentifier.identifier(enumClass.getSimpleName(), Collections.emptyList()); // + "a";
			int count = repository.find(Integer.class, enumExists(enumClass, enumIdentifier), 1).get(0);
			if (count == 0) {
				logger.info("New enum: " + enumIdentifier);
				if (update) {
					repository.execute(sqlDialect.createEnum(enumClass, enumIdentifier));
				}
			} else {
				String getEnumValues = selectEnumValues(enumClass, enumIdentifier);
				List<String> existingEnumValues = repository.find(String.class, getEnumValues, 10000);
				List<Enum> enumValues = EnumUtils.valueList(enumClass);
				for (Enum enmValue : enumValues) {
					if (!existingEnumValues.contains(enmValue.name())) {
						logger.info("New enum value: " + enumIdentifier + "." + enmValue.name());
						if (update) {
							repository.execute(addEnumValue(enumClass, enumIdentifier, enmValue.name()));
						}
					}
				}
			}
		}
	}

	protected String tableExists(Class<?> clazz, String identifier) {
		return "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = current_schema AND table_name = '" + identifier + "'";
	}

	protected String enumExists(Class<?> clazz, String identifier) {
		return "SELECT COUNT(*) FROM pg_type WHERE typcategory = 'E' AND typname = '" + identifier + "'";
	}
	
	protected String selectEnumValues(Class<?> clazz, String enumIdentifier) {
		return "SELECT unnest(enum_range(NULL::" + enumIdentifier + "))";
	}
	
	protected String addEnumValue(Class<?> clazz, String enumIdentifier, String value) {
		return "ALTER TYPE " + enumIdentifier + " ADD VALUE '" + value + "'";
	}
	
}
