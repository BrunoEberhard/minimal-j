package org.minimalj.example.shoppingcart;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.repository.memory.InMemoryRepository;
import org.minimalj.rest.RestServer;

public class ShoppingCartApplication extends Application {

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class[] { Product.class };
	}

	public static void main(String[] args) {
		Configuration.set("MjRepository", InMemoryRepository.class.getName());

		RestServer.start(new ShoppingCartApplication());
	}
}
