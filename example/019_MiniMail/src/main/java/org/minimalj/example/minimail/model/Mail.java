package org.minimalj.example.minimail.model;

import java.time.LocalDate;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;

public class Mail  {
	public static final Mail $ = Keys.of(Mail.class);

	public Object id;
	
	public final EMailAddress from = new EMailAddress();
	public final EMailAddress to = new EMailAddress();
	
	@Size(2000)
	@NotEmpty
	public String subject;
	
	public LocalDate date;
	
	@Size(20000)
	@NotEmpty
	public String text;
	
	
}
