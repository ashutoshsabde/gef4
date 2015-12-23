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

import com.google.common.collect.SetMultimap;

import javafx.beans.Observable;

/**
 * An {@link ObservableSetMultimap} is a specific {@link SetMultimap} that
 * allows observers to track changes by registering
 * {@link SetMultimapChangeListener}s.
 * 
 * @author anyssen
 *
 * @param <K>
 *            The key type of the {@link ObservableSetMultimap}.
 * @param <V>
 *            The value type of the {@link ObservableSetMultimap}.
 */
public interface ObservableSetMultimap<K, V>
		extends SetMultimap<K, V>, Observable {

	/**
	 * Adds a {@link SetMultimapChangeListener} to this
	 * {@link ObservableSetMultimap}. If the same listener is registered more
	 * than once, it will be notified more than once.
	 * 
	 * @param listener
	 *            The {@link SetMultimapChangeListener} to add.
	 */
	public void addListener(
			SetMultimapChangeListener<? super K, ? super V> listener);

	/**
	 * Removes a {@link SetMultimapChangeListener} from this
	 * {@link ObservableSetMultimap}. Will do nothing if the listener was not
	 * attached to this {@link ObservableSetMultimap}. If it was added more than
	 * once, then only the first occurrence will be removed.
	 * 
	 * @param listener
	 *            The {@link SetMultimapChangeListener} to remove.
	 */
	public void removeListener(
			SetMultimapChangeListener<? super K, ? super V> listener);
}
