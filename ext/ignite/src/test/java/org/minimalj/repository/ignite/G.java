package org.minimalj.repository.ignite;

import org.apache.ignite.cache.query.annotations.QuerySqlField;
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

	@Size(20) @Searched @QuerySqlField
	public String g;
}
