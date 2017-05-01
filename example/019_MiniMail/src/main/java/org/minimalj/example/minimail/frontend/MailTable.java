package org.minimalj.example.minimail.frontend;

import java.util.Collections;
import java.util.List;

import org.minimalj.example.minimail.model.Mail;
import org.minimalj.frontend.page.TablePage;

public class MailTable extends TablePage<Mail> {

	public static final Object[] keys = new Object[]{Mail.$.from, Mail.$.to, Mail.$.date, Mail.$.subject};
	
	public MailTable() {
		super(keys);
	}

	@Override
	protected List<Mail> load() {
		return Collections.emptyList();
	}

}
