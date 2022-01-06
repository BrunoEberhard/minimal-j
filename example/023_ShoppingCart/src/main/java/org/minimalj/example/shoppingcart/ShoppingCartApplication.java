package org.minimalj.example.shoppingcart;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.impl.web.MjHttpHandler;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.repository.Repository;
import org.minimalj.repository.memory.InMemoryRepository;
import org.minimalj.rest.RestHttpHandler;

public class ShoppingCartApplication extends WebApplication {

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class[] { Product.class };
	}
	
	@Override
	public Repository createRepository() {
		return new InMemoryRepository(this);
	}

	@Override
	public MjHttpHandler createHttpHandler() {
		return new RestHttpHandler();
	}
	
	public static void main(String[] args) {
	}
}
