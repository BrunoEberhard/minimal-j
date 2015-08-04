package org.minimalj.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReservedDbWords {
	private static final Logger logger = Logger.getLogger(ReservedDbWords.class.getName());
	
	public static final Collection<String> reservedDbWords = Collections.unmodifiableCollection(loadReservedDbWords());
	
	private static Collection<String> loadReservedDbWords() {
		Collection<String> reservedDbWords = new HashSet<>();
		String fileName = System.getProperty("MjReservedDbWordsFile", "reservedDbWords.txt");
		try (InputStreamReader isr = new InputStreamReader(ReservedDbWords.class.getResourceAsStream(fileName))) {
			try (BufferedReader r = new BufferedReader(isr)) {
				while (r.ready()) {
					String line = r.readLine();
					if (!line.startsWith("#")) {
						String[] words = line.split(" ");
						for (String word : words) {
							reservedDbWords.add(word);
						}
					}
				}
			} 
		} catch (NullPointerException e) {
			logger.severe("reservedDbWords.txt not found. Maybe something is wrong with the classpath");				
		} catch (IOException e) {
			logger.log(Level.SEVERE, "reservedDbWords.txt could not be read", e);		
		}
		return reservedDbWords;
	}
	
}
