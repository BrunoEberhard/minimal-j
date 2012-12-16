package ch.openech.test.db;

import ch.openech.mj.model.annotation.Size;


public class B {

	public B() {
		// needed for reflection constructor
	}
	
	public B(String bName) {
		this.bName = bName;
	}
	
	@Size(30)
	public String bName;
}
