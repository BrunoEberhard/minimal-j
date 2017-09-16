package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.IContent;

public class JsonQueryContent extends JsonComponent implements IContent {

	public JsonQueryContent(String caption) {
		super("Query");
		put("caption", caption);
	}
}
