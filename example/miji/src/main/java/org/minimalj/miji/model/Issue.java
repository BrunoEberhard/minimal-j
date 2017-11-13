package org.minimalj.miji.model;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.security.model.User;

public class Issue {

	public static final Issue $ = Keys.of(Issue.class);
	
	public Object id;
	
	public Long jiraId;
	@Size(255)
	public String key;
	
	@Size(2048)
	public String summary, description;
	
	
	@Size(2048)
	public String url;
	
	public Issue parent;
	public User assignee;
	
}
