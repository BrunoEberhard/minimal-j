package org.minimalj.autofill;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

// http://en.wikipedia.org/wiki/List_of_companies_of_Switzerland
public class OrganisationNameGenerator {

	private static List<String> names = new ArrayList<String>(200);
	private static int pos = -1;
	
	public static String getName() {
		if (names.isEmpty()) readNames();
		pos = (pos + 1) % names.size();
		return names.get(pos);
	}
	
	private static void readNames() {
		try {
			InputStream inputStream = OrganisationNameGenerator.class.getResourceAsStream("/ch/openech/resources/firmen.txt");
			readNames(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void readNames(InputStream fis) throws Exception {
		Scanner scanner = new Scanner(fis, "UTF-8");
		scanner.useDelimiter("\n");
		while (scanner.hasNextLine()) {
			String name = scanner.nextLine();
			names.add(name);
		}
		Collections.shuffle(names);
		scanner.close();
	}
	
}
