package org.minimalj.repository.sql.relation;

import org.minimalj.model.Code;


public class TestElementCode implements Code {

	public TestElementCode() {
		// needed for reflection constructor
	}

	public TestElementCode(Integer id, Integer value) {
		this.id = id;
		this.value = value;
	}

	public Integer id;
	
	public Integer value;
}
