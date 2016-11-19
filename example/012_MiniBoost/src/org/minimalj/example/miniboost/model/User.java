package org.minimalj.example.miniboost.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.Keys;
import org.minimalj.model.View;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;
import org.minimalj.security.Password;

public class User {
	public static final User $ = Keys.of(User.class);
	
//	@Size(255)
//	public char[] password;
	public final Password password= new Password();
	
	@NotEmpty
	public String loginname;

	public final List<Role> roles = new ArrayList<>();
	
	@Size(50)
	public String firstname, lastname;
	@Size(2)
	public String country;
	@Size(255) @NotEmpty
	public String email;
	@Size(50)
	public String firmname1, firmname2, firmname3;
	public final Address firm = new Address();
	public BigDecimal firmNo;
	@Size(255)
	public String verificationUid;
	public LocalDateTime verificationUidExpired;
	@NotEmpty
	public boolean verified;
	@Size(255)
	public String createdIp;
	@NotEmpty
	public Long userId;
	public LocalDateTime createdDate;
	public LocalDateTime editDate;

	@NotEmpty
	public Boolean enabled = false;
	@NotEmpty
	public Boolean accountnonexpired = true;
	@NotEmpty
	public Boolean credentialsnonexpired = true;
	@NotEmpty
	public Boolean accountnonlocked = true;
	public LocalDateTime lastPasswordChange;
	@NotEmpty
	public Boolean newsletter = false;
		
}
