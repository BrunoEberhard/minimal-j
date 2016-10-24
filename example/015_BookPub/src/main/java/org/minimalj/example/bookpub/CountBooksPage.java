package org.minimalj.example.bookpub;

import org.minimalj.backend.Backend;
import org.minimalj.example.bookpub.entity.Book;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.page.Page;
import org.minimalj.persistence.criteria.By;

public class CountBooksPage extends Page {

	@Override
	public IContent getContent() {
		return Frontend.getInstance().createHtmlContent("<html>Books: " + Backend.read(Book.class, By.all(), Integer.MAX_VALUE).size()+"</html>");
	}

}
