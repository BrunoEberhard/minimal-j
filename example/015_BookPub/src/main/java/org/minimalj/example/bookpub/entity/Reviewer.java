package org.minimalj.example.bookpub.entity;

import org.minimalj.model.annotation.Size;

public class Reviewer {
	public Object id;

	@Size(255)
	public String firstName, lastName;
}
