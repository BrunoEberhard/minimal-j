package org.minimalj.frontend.impl.cheerpj;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.minimalj.frontend.impl.json.JsonPageManager;

public class Cheerpj {

	private static JsonPageManager pageManager = new JsonPageManager();
	
	public static String receiveMessage(String inputString) {
		try {
			return pageManager.handle(inputString);
		} catch (Throwable t) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.flush();
			sw.flush();
			return sw.toString();
		}
	}

}
