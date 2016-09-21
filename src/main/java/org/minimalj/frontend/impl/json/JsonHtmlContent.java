package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.IContent;

public class JsonHtmlContent extends JsonComponent implements IContent {

	public JsonHtmlContent(String htmlOrUrl) {
		super(htmlOrUrl.startsWith("<") ? "Html" : "Url", false);
		put("htmlOrUrl", htmlOrUrl);
	}
	
}
