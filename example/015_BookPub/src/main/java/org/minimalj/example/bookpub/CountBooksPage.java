package org.minimalj.example.bookpub;

import org.minimalj.backend.Backend;
import org.minimalj.example.bookpub.entity.Book;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.page.Page;
import org.minimalj.repository.query.By;

public class CountBooksPage implements Page {

	@Override
	public IContent getContent() {
		return Frontend.getInstance().createHtmlContent("<html>Books: " + Backend.find(Book.class, By.all()).size()+"</html>");
	}

}
