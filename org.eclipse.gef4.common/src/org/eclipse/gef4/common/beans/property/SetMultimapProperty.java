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

import org.eclipse.gef4.common.beans.value.WritableSetMultimapValue;
import org.eclipse.gef4.common.collections.ObservableSetMultimap;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;

/**
 * This class provides a full implementation of a {@link Property} wrapping a
 * {@link ObservableSetMultimap}.
 * 
 * @param <K>
 *            The key type of the {@link ObservableSetMultimap}.
 * @param <V>
 *            The value type of the {@link ObservableSetMultimap}.
 * 
 */
public abstract class SetMultimapProperty<K, V>
		extends ReadOnlySetMultimapProperty<K, V> implements
		Property<ObservableSetMultimap<K, V>>, WritableSetMultimapValue<K, V> {

	@Override
	public void setValue(ObservableSetMultimap<K, V> v) {
		set(v);
	}

	@Override
	public void bindBidirectional(Property<ObservableSetMultimap<K, V>> other) {
		Bindings.bindBidirectional(this, other);
	}

	@Override
	public void unbindBidirectional(
			Property<ObservableSetMultimap<K, V>> other) {
		Bindings.unbindBidirectional(this, other);
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder(
				this.getClass().getSimpleName() + " [");
		final Object bean = getBean();
		if (bean != null) {
			result.append("bean: ").append(bean).append(", ");
		}
		final String name = getName();
		if ((name != null) && (!name.equals(""))) {
			result.append("name: ").append(name).append(", ");
		}
		appendValueToString(result);
		return result.append("]").toString();
	}

	/**
	 * Appends a representation of this {@link SetMultimapProperty}'s value to
	 * the given {@link StringBuilder}. Gets called from {@link #toString()} to
	 * allow subclasses to provide a changed value representation.
	 * 
	 * @param result
	 *            A {@link StringBuilder} to append the value representation to.
	 */
	protected void appendValueToString(final StringBuilder result) {
		result.append("value: ").append(get());
	}
}
