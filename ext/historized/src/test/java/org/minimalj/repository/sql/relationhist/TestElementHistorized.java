package org.minimalj.repository.sql.relationhist;

import java.util.List;

import org.minimalj.model.annotation.Size;


public class TestElementHistorized {

	public TestElementHistorized() {
		// needed for reflection constructor
	}
	
	public TestElementHistorized(String name) {
		this.name = name;
	}
	
	public Object id;
	public int version;
	public boolean historized;

	public List<TestElementC> list;
	
	@Size(30)
	public String name;
}
