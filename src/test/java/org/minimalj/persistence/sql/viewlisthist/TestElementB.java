package org.minimalj.persistence.sql.viewlisthist;

import java.util.List;

import org.minimalj.model.annotation.Size;


public class TestElementB {

	public TestElementB() {
		// needed for reflection constructor
	}
	
	public TestElementB(String name) {
		this.name = name;
	}
	
	public Object id;
	public int version;
	public boolean historized;

	public List<TestElementC> list;
	
	@Size(30)
	public String name;
}
