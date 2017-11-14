package org.minimalj.miji.backend;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

import org.junit.Test;

public class IssueTest {

	@Test
	public void testReadIssue() throws Exception {
		
		String userpass = "not to checkin";
		String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes("utf-8"));

		URL url = new URL("https://dm-informatics.atlassian.net/rest/api/2/issue/WCAT-2953");
		URLConnection uc = url.openConnection();
		
		uc.setRequestProperty ("Authorization", basicAuth);
		InputStream in = uc.getInputStream();
		
		// Issue issue = new IssueMapper().map(JsonReader.read(in));
		
		// System.out.println(issue.key + " " + issue.summary);
	}
}
