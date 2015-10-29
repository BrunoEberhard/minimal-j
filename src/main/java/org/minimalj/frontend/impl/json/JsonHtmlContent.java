package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.IContent;

public class JsonHtmlContent extends JsonComponent implements IContent {
	private static final long serialVersionUID = 1L;
	
	public JsonHtmlContent(String htmlOrUrl) {
		super(htmlOrUrl.startsWith("<") ? "Html" : "Url", false);
		put("htmlOrUrl", htmlOrUrl);
	}
	
}
