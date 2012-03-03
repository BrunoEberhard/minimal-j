package ch.openech.test.db;

import ch.openech.mj.db.model.annotation.Varchar;

public class C {

	public C() {
		// needed for reflection constructor
	}
	
	public C(String cName) {
		this.cName = cName;
	}
	
	@Varchar
	public String cName;
	
}
