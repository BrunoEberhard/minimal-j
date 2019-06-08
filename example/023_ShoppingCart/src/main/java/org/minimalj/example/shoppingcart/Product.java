package org.minimalj.example.shoppingcart;

import org.minimalj.model.annotation.Size;

public class Product {

	public Object id;

	@Size(1024)
	public String description;

}
