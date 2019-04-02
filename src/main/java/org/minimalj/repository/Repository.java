package org.minimalj.repository;

import java.util.List;
import java.util.logging.Logger;

import org.minimalj.application.Configuration;
import org.minimalj.model.Model;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.Query;
import org.minimalj.repository.sql.SqlRepository;

/**
 * The common interface of all types of repositories. Note that specific implementations
 * can have more methods. See for example the <code>execute</code> methods in SqlRepository.
 * 
 * A repository may use an arbitrary class for id fields. But it must be able to handle
 * reads and finds with ids converted to a String.
 *
 */
public interface Repository {
	public static final Logger logger = Logger.getLogger(Repository.class.getName());

	public static Repository create(Model model) {
		if (Configuration.available("MjRepository")) {
			return Configuration.getClazz("MjRepository", Repository.class, model);
		}

		return new SqlRepository(model);
	}

	// 
	
	public <T> T read(Class<T> clazz, Object id);

	public <T> List<T> find(Class<T> clazz, Query query);
	
	public <T> long count(Class<T> clazz, Criteria criteria);

	public <T> Object insert(T object);

	public <T> void update(T object);

	public <T> void delete(T object);

	public <T> int delete(Class<T> clazz, Criteria criteria);

}
