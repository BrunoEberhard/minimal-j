package org.minimalj.example.minimail.model;

import java.time.LocalDate;

import org.minimalj.model.View;
import org.minimalj.model.annotation.Size;

public class MailHeader implements View<Mail> {

	public final EMailAddress from = new EMailAddress();
	public final EMailAddress to = new EMailAddress();
	
	@Size(2000)
	public String subject;
	
	public LocalDate date;

}
