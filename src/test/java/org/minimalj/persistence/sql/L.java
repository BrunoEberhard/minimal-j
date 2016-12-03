package org.minimalj.persistence.sql;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class L {

	public static final L $ = Keys.of(L.class);
	
	public Object id;
	public int version;
	public boolean historized;

	@Size(30)
	public String aVeryLongFieldNameAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyz;

	@Size(30)
	public String aVeryLongFieldNameAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyzAbcdefghijklmnopqrstuvwyz2;

}
