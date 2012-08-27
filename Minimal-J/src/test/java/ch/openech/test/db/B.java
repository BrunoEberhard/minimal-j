package ch.openech.test.db;

import static ch.openech.mj.db.model.annotation.PredefinedFormat.String30;
import ch.openech.mj.db.model.annotation.Is;


public class B {

	public B() {
		// needed for reflection constructor
	}
	
	public B(String bName) {
		this.bName = bName;
	}
	
	@Is(String30)
	public String bName;
}
