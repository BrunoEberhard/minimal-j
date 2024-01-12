package org.minimalj.repository.sql;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;

public class G {
	public static final G $ = Keys.of(G.class);
	
	public G() {
		// needed for reflection constructor
	}

	public G(String g) {
		this.g = g;
	}

	public Object id;

	@Size(20) @Searched
	public String g;

	public K k;
	
	public final List<K> kList = new ArrayList<>();
}
