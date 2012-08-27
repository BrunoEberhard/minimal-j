package ch.openech.test.db;

import static ch.openech.mj.db.model.annotation.PredefinedFormat.String30;
import ch.openech.mj.db.model.annotation.Is;

public class C {

	public C() {
		// needed for reflection constructor
	}
	
	public C(String cName) {
		this.cName = cName;
	}
	
	@Is(String30)
	public String cName;
	
}
