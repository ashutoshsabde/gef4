/******************************************************************************
 * Copyright (c) 2015 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.gef4.common.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;

import javafx.beans.InvalidationListener;

/**
 * A wrapper class for a {@link SetMultimap} that adds observability.
 * 
 * @param <K>
 *            The key type of the {@link SetMultimap}.
 * @param <V>
 *            The value type of the {@link SetMultimap}.
 *
 * @author anyssen
 */
public class ObservableSetMultimapWrapper<K, V>
		implements ObservableSetMultimap<K, V> {

	private Multiset<InvalidationListener> invalidationListeners = HashMultiset
			.create();
	private Multiset<SetMultimapChangeListener<? super K, ? super V>> setMultimapChangeListeners = HashMultiset
			.create();

	@Override
	public Set<V> get(K key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<V> removeAll(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<V> replaceValues(K key, Iterable<? extends V> values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Entry<K, V>> entries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<K, Collection<V>> asMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsEntry(Object key, Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean put(K key, V value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(Object key, Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean putAll(K key, Iterable<? extends V> values) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<K> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Multiset<K> keys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<V> values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addListener(InvalidationListener listener) {
		invalidationListeners.add(listener);

	}

	@Override
	public void removeListener(InvalidationListener listener) {
		invalidationListeners.remove(listener);

	}

	@Override
	public void addListener(
			SetMultimapChangeListener<? super K, ? super V> listener) {
		setMultimapChangeListeners.add(listener);
	}

	@Override
	public void removeListener(
			SetMultimapChangeListener<? super K, ? super V> listener) {
		setMultimapChangeListeners.remove(listener);
	}

}
