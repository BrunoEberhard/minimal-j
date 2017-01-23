package org.minimalj.repository.sql;

import java.util.Set;

import org.minimalj.util.ReservedSqlWords;

public class SqlIdentifier {

	private static String avoidReservedSqlWords(String identifier, int maxLength) {
		if (ReservedSqlWords.reservedSqlWords.contains(identifier)) {
			if (identifier.length() == maxLength) {
				identifier = identifier.substring(0, identifier.length() - 1);
			}
			identifier = identifier + "_";
		}
		return identifier;
	}

	public static String buildIdentifier(String identifier, int maxLength, Set<String> alreadyUsedIdentifiers) {
		identifier = identifier.toUpperCase();
		identifier = cutToMaxLength(identifier, maxLength);
		identifier = avoidReservedSqlWords(identifier, maxLength);
		identifier = resolveIdentifierConflicts(alreadyUsedIdentifiers, identifier);
		return identifier;
	}

	private static String cutToMaxLength(String fieldName, int maxLength) {
		if (fieldName.length() > maxLength) {
			fieldName = fieldName.substring(0, maxLength);
		}
		return fieldName;
	}

	private static String resolveIdentifierConflicts(Set<String> alreadyUsedIdentifiers, String identifier) {
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
