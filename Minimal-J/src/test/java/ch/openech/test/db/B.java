package ch.openech.test.db;

import ch.openech.mj.db.model.annotation.Varchar;


public class B {

	public B() {
		// needed for reflection constructor
	}
	
	public B(String bName) {
		this.bName = bName;
	}
	
	@Varchar
	public String bName;
}
