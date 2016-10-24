package org.minimalj.example.bookpub.entity;

import java.util.List;

import org.minimalj.model.annotation.Size;

public class Book {
	public Object id;

	@Size(255)
	public String isbn, title, description;
	
	public Author author;
	
	public Publisher publisher;
	
	public List<Reviewer> reviewers;
	
}
