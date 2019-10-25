package org.minimalj.frontend.impl.web;

import org.minimalj.application.Application;

public abstract class WebApplication extends Application {

	public boolean handle(MjHttpExchange exchange) {
		return false;
	}

	public String pathToMinimalJ() {
		return "/";
	}
}
