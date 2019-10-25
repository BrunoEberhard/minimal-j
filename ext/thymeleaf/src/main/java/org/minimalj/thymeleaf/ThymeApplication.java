package org.minimalj.thymeleaf;

import java.lang.reflect.Method;
import java.util.Collection;

import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.WebApplication;

public class ThymeApplication extends WebApplication {
	private final String basePath;
	private final Collection<Class<?>> classes;

	public ThymeApplication(String basePath, Collection<Class<?>> classes) {
		this.basePath = basePath;
		this.classes = classes;
	}

	@Override
	public boolean handle(MjHttpExchange exchange) {
		String path = exchange.getPath();

		// 1. Nach Methoden suchen, die auf einen Path spezialisiert sind
		if (path.startsWith(basePath)) {
			Method method = null; // findMethodInClasses(path)
			if (method != null) {
				// ..
			}
		}

		// 2. Nach resourcen suchen, die zu dem Pfad passen

		return super.handle(exchange);
	}



}
