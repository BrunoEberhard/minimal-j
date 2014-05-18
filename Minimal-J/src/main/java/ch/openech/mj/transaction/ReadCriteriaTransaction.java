package ch.openech.mj.transaction;

import java.io.Serializable;
import java.util.List;

import ch.openech.mj.backend.Backend;
import ch.openech.mj.criteria.Criteria;
import ch.openech.mj.util.SerializationContainer;

public class ReadCriteriaTransaction<T> implements Transaction<Serializable> {
	private static final long serialVersionUID = 1L;

	private final Class<T> clazz;
	private final Criteria criteria;

	public ReadCriteriaTransaction(Class<T> clazz, Criteria criteria) {
		this.clazz = clazz;
		this.criteria = criteria;
	}

	@Override
	public Serializable execute(Backend backend) {
		List<T>	result = backend.read(clazz, criteria);
		return (Serializable) SerializationContainer.wrap(result); // TODO wrap should return SerializableList
	}

}