package org.minimalj.example.helloworld2;

import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.MjHttpHandler;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.frontend.page.Page;

public class HelloWorldWebApplication extends WebApplication {

	@Override
	protected MjHttpHandler createHttpHandler() {
		return new MjHttpHandler() {
			
			@Override
			public void handle(MjHttpExchange exchange) {
				if (exchange.getPath().equals("/")) {
					exchange.sendResponse(200, "This text is part of the web application", "plain/txt");
				} else {
					exchange.sendResponse(404, "This url is not available", "plain/txt");
				}
			}
		};
	}
	
	@Override
	protected String getMjPath() {
		return "/mj/";
	}

	@Override
	public Page createDefaultPage() {
		return new HelloWorldPage();
	}
	
}
