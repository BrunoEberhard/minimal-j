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
	private final ReferenceQueue<K> queue = new ReferenceQueue<>();
	private final Map<IdentityWeakReference, V> backingStore = new HashMap<>();

	@Override
	public void clear() {
		backingStore.clear();
		reap();
	}

	@Override
	public boolean containsKey(Object key) {
		reap();
		return backingStore.containsKey(new IdentityWeakReference(key));
	}

	@Override
	public boolean containsValue(Object value) {
		reap();
		return backingStore.containsValue(value);
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		reap();
		Set<Map.Entry<K, V>> ret = new HashSet<>();
		for (Map.Entry<IdentityWeakReference, V> ref : backingStore.entrySet()) {
			final K key = ref.getKey().get();
			final V value = ref.getValue();
			Map.Entry<K, V> entry = new Map.Entry<K, V>() {
				@Override
				public K getKey() {
					return key;
				}

				@Override
				public V getValue() {
					return value;
				}

				@Override
				public V setValue(V value) {
					throw new UnsupportedOperationException();
				}
			};
			ret.add(entry);
		}
		return Collections.unmodifiableSet(ret);
	}

	@Override
	public Set<K> keySet() {
		reap();
		Set<K> ret = new HashSet<>();
		for (IdentityWeakReference ref : backingStore.keySet()) {
			ret.add(ref.get());
		}
		return Collections.unmodifiableSet(ret);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean equals(Object o) {
		return backingStore.equals(((WeakIdentityHashMap) o).backingStore);
	}

	@Override
	public V get(Object key) {
		reap();
		return backingStore.get(new IdentityWeakReference(key));
	}

	@Override
	public V put(K key, V value) {
		reap();
		return backingStore.put(new IdentityWeakReference(key), value);
	}

	@Override
	public int hashCode() {
		reap();
		return backingStore.hashCode();
	}

	@Override
	public boolean isEmpty() {
		reap();
		return backingStore.isEmpty();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void putAll(Map t) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V remove(Object key) {
		reap();
		return backingStore.remove(new IdentityWeakReference(key));
	}

	@Override
	public int size() {
		reap();
		return backingStore.size();
	}

	@Override
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

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			IdentityWeakReference ref = (IdentityWeakReference) o;
			return this.get() == ref.get();
		}
	}
}
