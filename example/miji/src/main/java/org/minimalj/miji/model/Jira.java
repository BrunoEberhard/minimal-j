package org.minimalj.miji.model;

import java.util.List;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.annotation.Sizes;

@Sizes(JiraSizes.class)
public class Jira {

	public static class Project {
		public static final Project $ = Keys.of(Project.class);

		public Object id;

		public String self;

		public String description;

		public String name;
		public String key;

		public List<IssueType> issueTypes;
	}

	public static class User {
		public static final User $ = Keys.of(User.class);

		public Object id;

		public String self;

		public String description;

		public String name, key;

		@Size(255)
		public String displayName, accountId, emailAddress;
	}

	public static class Issue {
		public static final Issue $ = Keys.of(Issue.class);

		public Object id;
		public String key;

		public String self;

		public String description, summary;

		public String url;

		public Issue parent;
		public User assignee, creator;

		public CommentList comment;
	}

	public static class IssueType {
		public static final Issue $ = Keys.of(Issue.class);

		public Object id;
		public String key;

		public String self;

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

		@Size(32767)
		public String body;
	}
}
