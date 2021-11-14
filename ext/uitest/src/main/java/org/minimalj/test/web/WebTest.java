package org.minimalj.test.web;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.web.WebServer;
import org.minimalj.test.TestUtil;
import org.minimalj.test.UiTestFacade;

public abstract class WebTest {

	private static WebTestFacade ui;
	private static Application application;

	public static UiTestFacade ui() {
		if (ui == null) {
			ui = new WebTestFacade();
		}
		return ui;
	}

	public static void start(Application application) {
		if (WebTest.application != application) {
			TestUtil.shutdown();
			Application.setInstance(application);
			WebTest.application = application;
			WebServer.start(application);
			if (ui != null) {
				ui.reload();
			}
		}
	}
	
	public static String escapeXpath(String input) {
		if (input.contains("'") && input.contains("\"")) {
			// this code is never used at the moment
			
			StringBuilder s = new StringBuilder();
			s.append("concat(");

			Character open = null;
			for (int i = 0; i < input.length(); i++) {
				char c = input.charAt(i);
				if (c == '\'') {
					if (open != null && open.equals('"')) {
						s.append(c);
					} else if (open != null && open.equals('\'')) {
						s.append("', \"'");
						open = '"';
					} else {
						s.append("\"'");
						open = '"';
					}
				} else if (c == '"') {
					if (open != null && open.equals('\'')) {
						s.append(c);
					} else if (open != null && open.equals('\"')) {
						s.append("\", '\"");
						open = '\'';
					} else {
						s.append("'\"");
						open = '\'';
					}
				} else {
					if (open == null) {
						s.append("\"").append(c);
						open = '"';
					} else {
						s.append(c);
					}
				}
			}

			if (open.equals('\'')) {
				s.append("')");
			} else if (open.equals('\"')) {
				s.append("\")");
			}
			
			return s.toString();
		} else if (input.contains("'")) {
			return "\"" + input + "\"";
		} else {
			return "'" + input + "'";
		}
	}
}
