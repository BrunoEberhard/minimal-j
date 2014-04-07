package ch.openech.test.db;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.annotation.Size;
import ch.openech.mj.model.annotation.View;

public class H {

	public static final H H_ = Keys.of(H.class);
	
	public H() {
		// needed for reflection constructor
	}

	public int id;

	@Size(20)
	public String name;
	
	@View
	public G g;
	
	public final I i = new I();
}
