package org.minimalj.miji.model;

import java.util.List;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class Jira {

	public static class Project {
		public static final Project $ = Keys.of(Project.class);

		public Object id;

		@Size(2048)
		public String self;

		@Size(2048)
		public String description;

		@Size(255)
		public String key;

		public List<IssueType> issueTypes;
	}

	public static class User {
		public static final User $ = Keys.of(User.class);

		public Object id;

		@Size(2048)
		public String self;

		@Size(2048)
		public String description;

		@Size(255)
		public String name, displayName, key, accountId, emailAddress;

	}

	public static class Issue {
		public static final Issue $ = Keys.of(Issue.class);

		public Object id;
		@Size(255)
		public String key;

		@Size(2048)
		public String self;

		@Size(2048)
		public String description, summary;

		@Size(2048)
		public String url;

		public Issue parent;
		public User assignee;

		public CommentList comment;
	}

	public static class IssueType {
		public static final Issue $ = Keys.of(Issue.class);

		public Object id;
		@Size(255)
		public String key;

		@Size(2048)
		public String self;

		@Size(2048)
		public String description, name;

		@Size(2048)
		public String iconUrl;

		public Boolean subtask;

		public Long avatarId;
	}

	public static class CommentList {

		public List<Comment> comments;

		public final Range range = new Range();
	}

	public static class Range {

		public Long maxResults, total, startAt;
	}

	public static class Comment {

		public Object id;

		@Size(2048)
		public String body;
	}
}
