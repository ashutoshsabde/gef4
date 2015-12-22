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
package org.eclipse.gef4.common.beans.property;

import java.lang.ref.WeakReference;
import java.util.Map.Entry;

import org.eclipse.gef4.common.beans.binding.SetMultimapExpression;
import org.eclipse.gef4.common.collections.ObservableSetMultimap;
import org.eclipse.gef4.common.collections.SetMultimapChangeListener;

import com.google.common.collect.SetMultimap;

import javafx.beans.WeakListener;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyProperty;

/**
 * Abstract base class for all read-only properties wrapping an
 * {@link ObservableSetMultimap}.
 * 
 * @param <K>
 *            The key type of the {@link ObservableSetMultimap}.
 * @param <V>
 *            The value type of the {@link ObservableSetMultimap}.
 * 
 * @author anyssen
 */
public abstract class ReadOnlySetMultimapProperty<K, V>
		extends SetMultimapExpression<K, V>
		implements ReadOnlyProperty<ObservableSetMultimap<K, V>> {

	private static class BidirectionalSetMultimapContentBinding<K, V>
			implements SetMultimapChangeListener<K, V>, WeakListener {

		private final WeakReference<ObservableSetMultimap<K, V>> map1Ref;
		private final WeakReference<ObservableSetMultimap<K, V>> map2Ref;

		private boolean updating = false;

		public BidirectionalSetMultimapContentBinding(
				ObservableSetMultimap<K, V> map1,
				ObservableSetMultimap<K, V> map2) {
			map1Ref = new WeakReference<>(map1);
			map2Ref = new WeakReference<>(map2);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}

			final Object map1 = map1Ref.get();
			final Object map2 = map2Ref.get();
			if ((map1 == null) || (map2 == null)) {
				return false;
			}

			if (other instanceof BidirectionalSetMultimapContentBinding) {
				final BidirectionalSetMultimapContentBinding<?, ?> otherBinding = (BidirectionalSetMultimapContentBinding<?, ?>) other;
				final Object otherMap1 = otherBinding.map1Ref.get();
				final Object otherMap2 = otherBinding.map2Ref.get();

				if ((otherMap1 == null) || (otherMap2 == null)) {
					return false;
				}
				// The actual direction of the bidirectional binding is not
				// significant, thus we can ignore it here
				if (((map1 == otherMap1) && (map2 == otherMap2))
						|| ((map1 == otherMap2) && (map2 == otherMap1))) {
					return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode() {
			final ObservableSetMultimap<K, V> map1 = map1Ref.get();
			final ObservableSetMultimap<K, V> map2 = map2Ref.get();
			final int hc1 = (map1 == null) ? 0 : map1.hashCode();
			final int hc2 = (map2 == null) ? 0 : map2.hashCode();
			return hc1 * hc2;
		}

		@Override
		public void onChanged(Change<? extends K, ? extends V> change) {
			if (!updating) {
				final ObservableSetMultimap<K, V> map1 = map1Ref.get();
				final ObservableSetMultimap<K, V> map2 = map2Ref.get();
				if ((map1 == null) || (map2 == null)) {
					if (map1 != null) {
						map1.removeListener(this);
					}
					if (map2 != null) {
						map2.removeListener(this);
					}
				} else {
					try {
						updating = true;
						final SetMultimap<K, V> dest = (map1 == change
								.getSource()) ? map2 : map1;
						if (change.wasRemoved()) {
							// TODO: compute an elementary change instead?
							for (V value : change.getValuesRemoved()) {
								dest.remove(change.getKey(), value);
							}
						}
						if (change.wasAdded()) {
							dest.putAll(change.getKey(),
									change.getValuesAdded());
						}
					} finally {
						updating = false;
					}
				}
			}
		}

		@Override
		public boolean wasGarbageCollected() {
			return (map1Ref.get() == null) || (map2Ref.get() == null);
		}
	}

	private static class UnidirectionalSetMultimapContentBinding<K, V>
			implements SetMultimapChangeListener<K, V>, WeakListener {

		private final WeakReference<SetMultimap<K, V>> mapRef;

		public UnidirectionalSetMultimapContentBinding(SetMultimap<K, V> map) {
			this.mapRef = new WeakReference<>(map);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}

			final SetMultimap<K, V> map = mapRef.get();
			if (map == null) {
				return false;
			}

			if (other instanceof UnidirectionalSetMultimapContentBinding) {
				final UnidirectionalSetMultimapContentBinding<?, ?> otherBinding = (UnidirectionalSetMultimapContentBinding<?, ?>) other;
				final SetMultimap<?, ?> otherMap = otherBinding.mapRef.get();
				return map == otherMap;
			}
			return false;
		}

		@Override
		public int hashCode() {
			final SetMultimap<K, V> map = mapRef.get();
			return (map == null) ? 0 : map.hashCode();
		}

		@Override
		public void onChanged(Change<? extends K, ? extends V> change) {
			final SetMultimap<K, V> dest = mapRef.get();
			if (dest == null) {
				change.getSource().removeListener(this);
			} else {
				if (change.wasRemoved()) {
					// TODO: compute an elementary change instead?
					for (V value : change.getValuesRemoved()) {
						dest.remove(change.getKey(), value);
					}
				}
				if (change.wasAdded()) {
					dest.putAll(change.getKey(), change.getValuesAdded());
				}
			}
		}

		@Override
		public boolean wasGarbageCollected() {
			return mapRef.get() == null;
		}
	}

	/**
	 * Creates a unidirectional content binding between the
	 * {@link ObservableSetMultimap}, that is wrapped in this
	 * {@link ReadOnlyListProperty}, and the given {@link ObservableSetMultimap}
	 * .
	 * <p>
	 * A content binding ensures that the content of the wrapped
	 * {@link ObservableSetMultimap} is the same as that of the other
	 * set-multimap. If the content of the other set multimap changes, the
	 * wrapped set multimap will be updated automatically. Once the wrapped
	 * multi-map is bound to another multi-map, it must not be changed directly.
	 *
	 * @param target
	 *            The {@link ObservableSetMultimap} this property should be
	 *            unidirectionally bound to.
	 */
	public void bindContent(ObservableSetMultimap<K, V> target) {
		if (target == null) {
			throw new IllegalArgumentException(
					"Binding target may not be null.");
		}
		if (target == this) {
			throw new IllegalArgumentException(
					"Cannot bind a property to itself.");
		}
		final UnidirectionalSetMultimapContentBinding<K, V> contentBinding = new UnidirectionalSetMultimapContentBinding<>(
				this);
		clear();
		putAll(target);
		target.removeListener(contentBinding);
		target.addListener(contentBinding);
	}

	/**
	 * Creates a bidirectional content binding of the
	 * {@link ObservableSetMultimap}, that is wrapped in this
	 * {@link ReadOnlyListProperty}, and the given {@link ObservableSetMultimap}
	 * .
	 * <p>
	 * A bidirectional content binding ensures that the content of the two
	 * set-multimaps are the same. If the content of one of the set-multimaps
	 * changes, the other one will be updated automatically.
	 *
	 * @param target
	 *            The {@link ObservableSetMultimap} this property should be
	 *            bidirectionally bound to.
	 */
	public void bindContentBidirectional(ObservableSetMultimap<K, V> target) {
		if (target == null) {
			throw new IllegalArgumentException(
					"Binding target may not be null.");
		}
		if (target == this) {
			throw new IllegalArgumentException(
					"Cannot bind a property to itself.");
		}
		final BidirectionalSetMultimapContentBinding<K, V> binding = new BidirectionalSetMultimapContentBinding<>(
				this, target);
		clear();
		putAll(target);
		addListener(binding);
		target.addListener(binding);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof SetMultimap)) {
			return false;
		}

		try {
			SetMultimap<K, V> otherMap = (SetMultimap<K, V>) obj;
			if (otherMap.size() != size())
				return false;
			for (Entry<K, V> e : entries()) {
				K key = e.getKey();
				V value = e.getValue();
				if (value == null) {
					if (!(otherMap.get(key) == null
							&& otherMap.containsKey(key)))
						return false;
				} else {
					if (!value.equals(otherMap.get(key)))
						return false;
				}
			}
		} catch (ClassCastException e) {
			return false;
		} catch (NullPointerException e) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int h = 0;
		for (Entry<K, V> e : entries()) {
			h += e.hashCode();
		}
		return h;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder(
				"ReadOnlySetMultimapProperty [");
		final Object bean = getBean();
		if (bean != null) {
			result.append("bean: ").append(bean).append(", ");
		}
		final String name = getName();
		if ((name != null) && !name.equals("")) {
			result.append("name: ").append(name).append(", ");
		}
		result.append("value: ").append(get()).append("]");
		return result.toString();
	}

	/**
	 * Deletes a content binding between the {@link ObservableSetMultimap}, that
	 * is wrapped in this {@link ReadOnlyListProperty}, and another
	 * {@link Object}.
	 *
	 * @param object
	 *            The {@link Object} to which the binding should be removed.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void unbindContent(Object object) {
		if (object == null) {
			throw new IllegalArgumentException(
					"Unbinding target may not be null.");
		}
		if (object == this) {
			throw new IllegalArgumentException(
					"Cannot unbind a property from itself.");
		}
		if (object instanceof ObservableSetMultimap) {
			((ObservableSetMultimap<?, ?>) object).removeListener(
					new UnidirectionalSetMultimapContentBinding(this));
		}
	}

	/**
	 * Deletes a bidirectional content binding between the
	 * {@link ObservableSetMultimap}, that is wrapped in this
	 * {@link ReadOnlyListProperty}, and another {@link Object}.
	 *
	 * @param object
	 *            The {@link Object} to which the bidirectional binding should
	 *            be removed.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void unbindContentBidirectional(Object object) {
		if (object == null) {
			throw new IllegalArgumentException(
					"Unbinding target may not be null.");
		}
		if (object == this) {
			throw new IllegalArgumentException(
					"Cannot unbind a property from itself.");
		}
		if (object instanceof ObservableSetMultimap) {
			final ObservableSetMultimap<?, ?> otherMap = (ObservableSetMultimap<?, ?>) object;
			final BidirectionalSetMultimapContentBinding binding = new BidirectionalSetMultimapContentBinding(
					this, otherMap);
			removeListener(binding);
			otherMap.removeListener(binding);
		}
	}

}
