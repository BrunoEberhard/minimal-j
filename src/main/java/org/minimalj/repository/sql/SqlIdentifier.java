package org.minimalj.repository.sql;

import java.util.HashSet;
import java.util.Set;

import org.minimalj.util.ReservedSqlWords;

public class SqlIdentifier {

	private final int maxIdentifierLength;
	
	private final Set<String> foreignKeyNames = new HashSet<>();
	private final Set<String> indexNames = new HashSet<>();

	public SqlIdentifier(int maxIdentifierLength) {
		this.maxIdentifierLength = maxIdentifierLength;
	}

	protected String identifier(String identifier, Set<String> alreadyUsedIdentifiers) {
		identifier = identifier.toUpperCase();
		identifier = cutToMaxLength(identifier);
		identifier = avoidReservedSqlWords(identifier);
		identifier = resolveIdentifierConflicts(alreadyUsedIdentifiers, identifier);
		return identifier;
	}
	
	public String table(String identifier, Set<String> alreadyUsedIdentifiers) {
		return identifier(identifier, alreadyUsedIdentifiers);
	}

	public final String column(String identifier, Set<String> alreadyUsedIdentifiers) {
		return column(identifier, alreadyUsedIdentifiers, null);
	}
	
	public String column(String identifier, Set<String> alreadyUsedIdentifiers, Class<?> fieldClass) {
		return identifier(identifier, alreadyUsedIdentifiers);
	}
	
	public final String constraint(String tableName, String column, String referencedTableName) {
		String name = "FK_" + tableName + "_" + column;
		name = identifier(name, foreignKeyNames);
		foreignKeyNames.add(name);
		return name;
	}

	public String index(String tableName, String indexedColumn) {
		String name = "IDX_" + tableName + "_" + indexedColumn;
		name = identifier(name, indexNames);
		indexNames.add(name);
		return name;
	}
	
	protected String cutToMaxLength(String fieldName) {
		if (fieldName.length() > maxIdentifierLength) {
			fieldName = fieldName.substring(0, maxIdentifierLength);
		}
		return fieldName;
	}

	protected String avoidReservedSqlWords(String identifier) {
		if (ReservedSqlWords.reservedSqlWords.contains(identifier)) {
			if (identifier.length() == maxIdentifierLength) {
				identifier = identifier.substring(0, identifier.length() - 1);
			}
			identifier = identifier + "_";
		}
		return identifier;
	}

	protected String resolveIdentifierConflicts(Set<String> alreadyUsedIdentifiers, String identifier) {
		if (alreadyUsedIdentifiers.contains(identifier)) {
			int i = 1;
			do {
				String number = Integer.toString(i);
				String tryFieldName = identifier.substring(0, Math.max(identifier.length() - number.length() - 1, 1)) + "_" + number;
				if (!alreadyUsedIdentifiers.contains(tryFieldName)) {
					identifier = tryFieldName;
					break;
				}
				i++;
			} while (true);
		}
		return identifier;
	}

}
