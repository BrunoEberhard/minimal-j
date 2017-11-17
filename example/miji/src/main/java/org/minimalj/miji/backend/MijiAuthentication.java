package org.minimalj.miji.backend;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.impl.json.JsonReader;
import org.minimalj.miji.model.Jira.Issue;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;
import org.minimalj.security.Authentication;
import org.minimalj.security.Subject;

public class MijiAuthentication extends Authentication {

	@Override
	public void login(LoginListener loginListener) {
		new MijiUserPasswordAction(loginListener).action();
	}
	
	public static class MijiUserPassword implements Serializable {
		private static final long serialVersionUID = 1L;

		public static final MijiUserPassword $ = Keys.of(MijiUserPassword.class);

		@Size(255) @NotEmpty
		public String url;
		
		@Size(255) @NotEmpty
		public String user;

		@Size(255)
		public String password;

	}
	
	public static class MijiSubject extends Subject {
		private static final long serialVersionUID = 1L;
		
		private final String url;
		private final String password;
		
		public MijiSubject(String url, String password) {
			this.url = url;
			this.password = password;
		}
		
		public String getUrl() {
			return url;
		}
		
		public String getPassword() {
			return password;
		}
	}
	
	public static class MijiUserPasswordAction extends Editor<MijiUserPassword, Subject> {

		private final LoginListener listener;
		
		public MijiUserPasswordAction(LoginListener listener) {
			this.listener = listener;
		}

		@Override
		protected MijiUserPassword createObject() {
			return new MijiUserPassword();
		}

		@Override
		protected Form<MijiUserPassword> createForm() {
			Form<MijiUserPassword> form = new Form<>();
			form.line(MijiUserPassword.$.url);
			form.line(MijiUserPassword.$.user);
			form.line(MijiUserPassword.$.password);
			return form;
		}

		@Override
		protected Subject save(MijiUserPassword userPassword) {
			if (ok(userPassword)) {
				MijiSubject subject = new MijiSubject(userPassword.url, userPassword.password);
				subject.setName(userPassword.user);
				return subject;
			} else {
				return null;
			}
		}

		@Override
		protected boolean closeWith(Subject subject) {
			if (subject != null) {
				return true;
			} else {
				Frontend.showError("Login failed");
				return false;
			}
		}
		
		@Override
		public void cancel() {
			listener.loginCancelled();
			super.cancel();
		}
		
		@Override
		protected void finished(Subject subject) {
			listener.loginSucceded(subject);
		}
	}
	
	protected static boolean ok(MijiUserPassword i) {
		try {
			String userpass = i.user + ":" + i.password;
			String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes("utf-8"));

			
			URL url = new URL(i.url + "rest/api/2/search?jql=assignee=" + i.user);
			URLConnection uc = url.openConnection();
			
			uc.setRequestProperty ("Authorization", basicAuth);
			InputStream in = uc.getInputStream();
			
			List<Issue> issues = new MijiJsonReader().issues(JsonReader.read(in));
			return true;
			
		} catch (Exception x) {
			x.printStackTrace();
			return false;
		}
	}

}
