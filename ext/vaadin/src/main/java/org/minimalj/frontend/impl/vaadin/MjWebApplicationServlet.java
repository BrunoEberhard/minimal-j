package org.minimalj.frontend.impl.vaadin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.util.LocaleContext;
import org.minimalj.util.LocaleContext.AcceptedLanguageLocaleSupplier;

public class MjWebApplicationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			LocaleContext.setLocale(new AcceptedLanguageLocaleSupplier(request.getHeader(AcceptedLanguageLocaleSupplier.ACCEPTED_LANGUAGE_HEADER)));
			HttpServletHttpExchange exchange = new HttpServletHttpExchange(request, response);
			WebApplication.handle(exchange);
		} finally {
			LocaleContext.resetLocale();
		}		
	}

}