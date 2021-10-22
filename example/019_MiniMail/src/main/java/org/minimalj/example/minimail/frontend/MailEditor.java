package org.minimalj.example.minimail.frontend;

import org.minimalj.backend.Backend;
import org.minimalj.example.minimail.backend.MailService;
import org.minimalj.example.minimail.model.Mail;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.StringFormElement;

public class MailEditor extends SimpleEditor<Mail> {

	@Override
	protected Mail createObject() {
		return new Mail();
	}

	@Override
	protected Form<Mail> createForm() {
		Form<Mail> form = new Form<>(Form.EDITABLE, 1, 300);
		form.line(new StringFormElement(Mail.$.from.address, StringFormElement.SINGLE_LINE));
		form.line(new StringFormElement(Mail.$.to.address, StringFormElement.SINGLE_LINE));
		form.line(Mail.$.date);
		form.line(new StringFormElement(Mail.$.subject, StringFormElement.SINGLE_LINE));
		form.line(Mail.$.text);
		return form;
	}

	@Override
	protected Mail save(Mail object) {
		MailService.sendMail(object);
		return Backend.save(object);
	}

}
