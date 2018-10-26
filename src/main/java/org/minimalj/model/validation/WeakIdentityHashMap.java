package org.minimalj.model.validation;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * Based on:
 * http://www.java2s.com/Code/Java/Collections-Data-Structure/ImplementsacombinationofWeakHashMapandIdentityHashMap.htm
 * 
 */
public class WeakIdentityHashMap<K, V> implements Map<K, V> {
	private final ReferenceQueue<K> queue = new ReferenceQueue<K>();
	private final Map<IdentityWeakReference, V> backingStore = new HashMap<IdentityWeakReference, V>();

	public void clear() {
		backingStore.clear();
		reap();
	}

	public boolean containsKey(Object key) {
		reap();
		return backingStore.containsKey(new IdentityWeakReference(key));
	}

	public boolean containsValue(Object value) {
		reap();
		return backingStore.containsValue(value);
	}

	public Set<Map.Entry<K, V>> entrySet() {
		reap();
		Set<Map.Entry<K, V>> ret = new HashSet<Map.Entry<K, V>>();
		for (Map.Entry<IdentityWeakReference, V> ref : backingStore.entrySet()) {
			final K key = ref.getKey().get();
			final V value = ref.getValue();
			Map.Entry<K, V> entry = new Map.Entry<K, V>() {
				public K getKey() {
					return key;
				}

				public V getValue() {
					return value;
				}

				public V setValue(V value) {
					throw new UnsupportedOperationException();
				}
			};
			ret.add(entry);
		}
		return Collections.unmodifiableSet(ret);
	}

	public Set<K> keySet() {
		reap();
		Set<K> ret = new HashSet<K>();
		for (IdentityWeakReference ref : backingStore.keySet()) {
			ret.add(ref.get());
		}
		return Collections.unmodifiableSet(ret);
	}

	@SuppressWarnings("rawtypes")
	public boolean equals(Object o) {
		return backingStore.equals(((WeakIdentityHashMap) o).backingStore);
	}

	public V get(Object key) {
		reap();
		return backingStore.get(new IdentityWeakReference(key));
	}

	public V put(K key, V value) {
		reap();
		return backingStore.put(new IdentityWeakReference(key), value);
	}

	public int hashCode() {
		reap();
		return backingStore.hashCode();
	}

	public boolean isEmpty() {
		reap();
		return backingStore.isEmpty();
	}

	@SuppressWarnings("rawtypes")
	public void putAll(Map t) {
		throw new UnsupportedOperationException();
	}

	public V remove(Object key) {
		reap();
		return backingStore.remove(new IdentityWeakReference(key));
	}

	public int size() {
		reap();
		return backingStore.size();
	}

	public Collection<V> values() {
		reap();
		return backingStore.values();
	}

	@SuppressWarnings("unchecked")
	private synchronized void reap() {
		Object zombie = queue.poll();

		while (zombie != null) {
			IdentityWeakReference victim = (IdentityWeakReference) zombie;
			backingStore.remove(victim);
			zombie = queue.poll();
		}
	}

	private class IdentityWeakReference extends WeakReference<K> {
		private final int hash;

		@SuppressWarnings("unchecked")
		private IdentityWeakReference(Object obj) {
			super((K) obj, queue);
			hash = System.identityHashCode(obj);
		}

		public int hashCode() {
			return hash;
		}

		@SuppressWarnings("unchecked")
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			IdentityWeakReference ref = (IdentityWeakReference) o;
			if (this.get() == ref.get()) {
				return true;
			}
			return false;
		}
	}
}
