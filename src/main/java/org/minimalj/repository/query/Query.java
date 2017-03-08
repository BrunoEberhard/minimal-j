package org.minimalj.repository.query;

import java.io.Serializable;

import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;

/**
 * The inner interfaces enforce the pattern: criteria(s) / order(s) / limit.<p>
 * 
 * <UL>
 * <LI>To a criteria a next criteria can be added but also an order or limit</LI>
 * <LI>To a order a next order can be added and also a limit</LI>
 * <LI>To a limit nothing can be added</LI>
 * </UL>
 * 
 */
public interface Query extends Serializable {
	
	public interface QueryLimitable extends Query {
		
		public default Query limit(int rows) {
			return new Limit(this, rows);
		}

		public default Query limit(Integer offset, int rows) {
			return new Limit(this, offset, rows);
		}
	}
	
	public interface QueryOrderable extends QueryLimitable {
		
		public default Order order(Object key) {
			return order(key, true);
		}
			
		public default Order order(Object key, boolean ascending) {
			PropertyInterface property = Keys.getProperty(key);
			String path = property.getPath();
			return new Order(this, path, ascending);
		}
	}
	
}