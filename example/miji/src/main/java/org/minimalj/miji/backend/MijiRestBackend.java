package org.minimalj.miji.backend;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.impl.json.JsonReader;
import org.minimalj.miji.backend.MijiAuthentication.MijiSubject;
import org.minimalj.miji.frontend.SimpleCriteria;
import org.minimalj.miji.model.Jira.Issue;
import org.minimalj.repository.Repository;
import org.minimalj.repository.query.Limit;
import org.minimalj.repository.query.Order;
import org.minimalj.repository.query.Query;
import org.minimalj.security.Authentication;
import org.minimalj.security.Subject;
import org.minimalj.util.StringUtils;

public class MijiRestBackend extends Backend {

	public MijiRestBackend() {
		setRepository(new MijiRestRepository());
	}
	
	@Override
	protected Authentication createAuthentication() {
		return new MijiAuthentication();
	}
	
	public class MijiRestRepository implements Repository {

		public <T> T read(Class<T> clazz, Object id) {
			if (clazz == Issue.class) {
				return (T) get(clazz, id);
			}
			return null;
		}

		public <T> List<T> find(Class<T> clazz, Query query) {
			String limitAndOrder = "";
			if (query instanceof Limit) {
				Limit limit = (Limit) query;
				limitAndOrder = "maxResults=" + limit.getRows();
				if (limit.getOffset() != null) {
					limitAndOrder += "&startAt=" + limit.getOffset();
				}
				query = limit.getQuery();
			}
			if (query instanceof Order) {
				Order order = (Order) query;
				if (!StringUtils.isEmpty(limitAndOrder)) {
					limitAndOrder += "&";
				}
				limitAndOrder += "orderBy=";
				limitAndOrder += order.isAscending() ? "+" : "-";
				limitAndOrder += order.getPath();
				while (query instanceof Order) {
					// at the moment skip further orderings
					query = ((Order) query).getQuery();
				}
			}
			
			if (!(query instanceof SimpleCriteria)) {
				throw new IllegalArgumentException("Only SimpleCriteria supported at the moment");
			}
			SimpleCriteria criteria = (SimpleCriteria) query;
			
			if (clazz == Issue.class) {
				return (List<T>) findIssues(criteria, limitAndOrder);
			}
			return null;
		}

		public <T> long count(Class<T> clazz, Query query) {
			// TODO Auto-generated method stub
			return 0;
		}

		public <T> Object insert(T object) {
			// TODO Auto-generated method stub
			return null;
		}

		public <T> void update(T object) {
			// TODO Auto-generated method stub
			
		}

		public <T> void delete(Class<T> clazz, Object id) {
			// TODO Auto-generated method stub
		}
	}
	
	private List<Issue> findIssues(SimpleCriteria criteria, String limitAndOrder) {
		try {
			MijiSubject subject = (MijiSubject) Subject.getCurrent();
			
			String userpass = subject.getName() + ":" + subject.getPassword();
			String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes("utf-8"));
			
			URL url = new URL(subject.getUrl() + "rest/api/2/search?jql=" + criteria.getKey() + "=" + criteria.getValue());
			URLConnection uc = url.openConnection();
			
			uc.setRequestProperty ("Authorization", basicAuth);
			InputStream in = uc.getInputStream();
			
			return MijiJsonReader.issues(JsonReader.read(new InputStreamReader(in, Charset.forName("UTF-8"))));
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
	
	private <T> T get(Class<T> clazz, Object id) {
		try {
			MijiSubject subject = (MijiSubject) Subject.getCurrent();
			
			String userpass = subject.getName() + ":" + subject.getPassword();
			String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes("utf-8"));
			
			URL url = new URL(subject.getUrl() + "rest/api/2/" + StringUtils.lowerFirstChar(clazz.getSimpleName()) + "/" + id);
			URLConnection uc = url.openConnection();
			
			uc.setRequestProperty ("Authorization", basicAuth);
			InputStream in = uc.getInputStream();
			
			return MijiJsonReader.read(clazz, JsonReader.read(new InputStreamReader(in, Charset.forName("UTF-8"))));
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
