package org.minimalj.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.Configuration;

public class ReservedSqlWords {
	private static final Logger logger = Logger.getLogger(ReservedSqlWords.class.getName());
	
	private static final Collection<String> reservedSqlWords = Collections.unmodifiableCollection(loadReservedSqlWords());
	
	private static Collection<String> loadReservedSqlWords() {
		Collection<String> reservedSqlWords = new HashSet<>();
		String fileName = Configuration.get("MjReservedSqlWordsFile", "reservedSqlWords.txt");
		try (InputStreamReader isr = new InputStreamReader(ReservedSqlWords.class.getResourceAsStream(fileName))) {
			try (BufferedReader r = new BufferedReader(isr)) {
				while (r.ready()) {
					String line = r.readLine().toUpperCase();
					if (!line.startsWith("#")) {
						String[] words = line.split(" ");
						Collections.addAll(reservedSqlWords, words);
					}
				}
			} 
		} catch (NullPointerException e) {
			logger.severe("reservedSqlWords.txt not found. Maybe something is wrong with the classpath");				
		} catch (IOException e) {
			logger.log(Level.SEVERE, "reservedSqlWords.txt could not be read", e);		
		}
		return reservedSqlWords;
	}
	
	public static boolean isReservedWord(String identifier) {
		return reservedSqlWords.contains(identifier.toUpperCase());
	}
	
}
