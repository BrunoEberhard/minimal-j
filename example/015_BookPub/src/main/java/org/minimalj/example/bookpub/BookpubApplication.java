package org.minimalj.example.bookpub;

import org.minimalj.application.Application;
import org.minimalj.example.bookpub.entity.Book;
import org.minimalj.frontend.impl.swing.Swing;
import org.minimalj.frontend.page.Page;

public class BookpubApplication extends Application {

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class[]{Book.class};
	}

	@Override
	public Page createDefaultPage() {
		return new CountBooksPage();
	}
	
	public static void main(String[] args) {
		Swing.start(new BookpubApplication());
	}
	
}
