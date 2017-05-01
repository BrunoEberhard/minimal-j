package org.minimalj.example.minimail.frontend;

import org.minimalj.example.minimail.model.Mail;
import org.minimalj.example.minimail.model.MailHeader;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.StringFormElement;

public class MailEditor extends Editor<Mail, MailHeader> {

	@Override
	protected Mail createObject() {
		return new Mail();
	}

	@Override
	protected Form<Mail> createForm() {
		Form<Mail> form = new Form<Mail>();
		form.line(new StringFormElement(Mail.$.from.address, StringFormElement.SINGLE_LINE));
		form.line(new StringFormElement(Mail.$.to.address, StringFormElement.SINGLE_LINE));
		form.line(Mail.$.date);
		form.line(new StringFormElement(Mail.$.subject, StringFormElement.SINGLE_LINE));
		form.line(Mail.$.text);
		return form;
	}

	@Override
	protected MailHeader save(Mail object) {
		return null;
	}

}
