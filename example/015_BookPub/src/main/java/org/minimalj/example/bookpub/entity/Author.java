package org.minimalj.example.bookpub.entity;

import java.util.List;

import org.minimalj.model.annotation.Size;

public class Author {
	public Object id;

	@Size(255)
	public String firstName, lastName;
	
	public List<Book> books;
}
