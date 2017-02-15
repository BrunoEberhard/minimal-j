package org.minimalj.repository.criteria;

import java.io.Serializable;

public class Sorting implements Serializable {

	private final String path;
	private final boolean ascending;
	
	public Sorting(String path, boolean ascending) {
		this.path = path;
		this.ascending = ascending;
	}
	
}