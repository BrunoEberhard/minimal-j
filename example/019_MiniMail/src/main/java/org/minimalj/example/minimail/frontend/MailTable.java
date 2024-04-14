package org.minimalj.example.minimail.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.minimail.model.Mail;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.repository.query.By;

public class MailTable extends TablePage<Mail> {

	@Override
	protected Object[] getColumns() {
		return new Object[] { Mail.$.from.address, Mail.$.to.address, Mail.$.date, Mail.$.subject };
	}
	
	@Override
	protected List<Mail> load() {
		return Backend.find(Mail.class, By.all());
	}

}
