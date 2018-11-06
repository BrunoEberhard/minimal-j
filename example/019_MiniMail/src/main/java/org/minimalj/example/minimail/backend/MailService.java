package org.minimalj.example.minimail.backend;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.minimalj.example.minimail.model.Mail;

public class MailService {
	
	private static final Logger LOGGER = Logger.getLogger(MailService.class.getName());

	public static void sendMail(Mail mail) throws MessagingException {
		Properties props = new Properties();
		try {
			props.load(MailService.class.getClassLoader().getResourceAsStream("mail.properties"));
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "unable to load smtp mail server properties, using default values instead", e);
			props.put("mail.smtp.host", "127.0.0.1");
			props.put("mail.smtp.port", 2500);
		}

		Session session = Session.getInstance(props, null);
		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(mail.from.address));
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail.to.address, false));
		msg.setText(mail.text);
		msg.setSubject(mail.subject);
		Transport.send(msg);
	}

}
