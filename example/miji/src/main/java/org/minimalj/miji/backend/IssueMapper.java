package org.minimalj.miji.backend;

import java.util.Map;

import org.minimalj.miji.model.Issue;

public class IssueMapper {

	public Issue map(Object input) {
		Issue issue = new Issue();
		if (input instanceof Map) {
			Map<Object, Object> map = (Map<Object, Object>) input;
			issue.key = (String) map.get("key");
			
			if (map.get("fields") instanceof Map) {
				mapFields(issue, (Map<Object, Object>) map.get("fields"));
			}
			
			
		} else {
			throw new IllegalArgumentException();
		}
		
		return issue;
	}

	private void mapFields(Issue issue, Map<Object, Object> map) {
		issue.summary = (String) map.get("summary");
		issue.description = (String) map.get("description");
	}
	
}
