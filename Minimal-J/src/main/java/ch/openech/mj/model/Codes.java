package ch.openech.mj.model;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import ch.openech.mj.db.model.Code;

public class Codes {

	private static Map<String, Code> codes = new HashMap<>();
	
	public static void addCodes(ResourceBundle resources) {
		Set<String> codeNames = new TreeSet<>();
		Enumeration<String> keys = resources.getKeys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			int pos = key.indexOf(".");
			if (pos > 0) {
				String codeName = key.substring(0, pos);
				codeNames.add(codeName);
			}
		}
		for (String codeName : codeNames) {
			if (codes.containsKey(codeName)) {
				throw new IllegalArgumentException("Double code: " + codeName);
			}
			codes.put(codeName, new Code(resources, codeName));
		}
	}
	
	public static Code getCode(String codeName) {
		return codes.get(codeName);
	}
	
}
