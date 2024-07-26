package org.minimalj.frontend.impl.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.page.Page;

public class JsonDashboardContent extends JsonComponent implements IContent {

	public JsonDashboardContent(List<Page> dashes) {
		super("Dashboard");
		put("dashes", dashes.stream().map(this::map).collect(Collectors.toList()));
	}
	
	private Map<String, Object> map(Page page) {
		Map<String, Object> json = new HashMap<>();
		json.put("title", page.getTitle());
		json.put("content", page.getContent());
		return json;
		
	}
}
