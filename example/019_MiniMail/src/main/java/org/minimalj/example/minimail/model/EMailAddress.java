package org.minimalj.example.minimail.model;

import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.validation.Validation;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.resources.Resources;

public class EMailAddress implements Validation {
	public static final EMailAddress $ = Keys.of(EMailAddress.class);

	@Size(2000)
	@NotEmpty
	public String address;
	
	@Override
	public List<ValidationMessage> validate() {
		if (address != null && !isValidEmailAddress(address)) {
			return Validation.message($.address, Resources.getString("Mail.address.validation.error"));
		}
		return null;
	}

	private static boolean isValidEmailAddress(String email) {
		boolean result = true;
		try {
			InternetAddress emailAddr = new InternetAddress(email);
			emailAddr.validate();
		} catch (AddressException ex) {
			result = false;
		}
		return result;
	}

}
