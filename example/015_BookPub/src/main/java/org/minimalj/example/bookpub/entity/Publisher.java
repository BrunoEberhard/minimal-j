package org.minimalj.example.bookpub.entity;

import java.util.List;

import org.minimalj.model.annotation.Size;

public class Publisher {
	public Object id;

	@Size(255)
	public String name;
	
	public List<Book> books;
}
