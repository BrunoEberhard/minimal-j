package org.minimalj.miji.backend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.repository.Repository;
import org.minimalj.repository.query.Query;

public class MijiRestBackend extends Backend {

	public MijiRestBackend() {
		setRepository(new MijiRestRepository());
	}
	
	public class MijiRestRepository implements Repository {

		public <T> T read(Class<T> clazz, Object id) {
			// TODO Auto-generated method stub
			return null;
		}

		public <T> List<T> find(Class<T> clazz, Query query) {
			// TODO Auto-generated method stub
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
}
