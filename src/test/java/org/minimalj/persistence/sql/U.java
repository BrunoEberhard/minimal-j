package org.minimalj.persistence.sql;

import java.time.LocalDateTime;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.annotation.TechnicalField;
import org.minimalj.model.annotation.TechnicalField.TechnicalFieldType;

public class U {
	public static final U $ = Keys.of(U.class);
	
	public Object id;
	public int version;
	public boolean historized;

	@Size(255)
	public String string;
	
	@TechnicalField(TechnicalFieldType.CREATE_DATE) 
	public LocalDateTime createDate;
	
	@TechnicalField(TechnicalFieldType.CREATE_USER) @Size(255)
	public String createUser;

	@TechnicalField(TechnicalFieldType.EDIT_DATE)
	public LocalDateTime editDate;
	
	@TechnicalField(TechnicalFieldType.EDIT_USER) @Size(255)
	public String editUser;

}