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
package org.eclipse.gef4.common.beans.value;

import org.eclipse.gef4.common.collections.ObservableSetMultimap;

import javafx.beans.value.ObservableObjectValue;

/**
 * An observable reference to an {@link ObservableSetMultimap}.
 * 
 * @author anyssen
 *
 * @param <K>
 *            The key type of the {@link ObservableSetMultimap}.
 * @param <V>
 *            The value type of the {@link ObservableSetMultimap}.
 */
public interface ObservableSetMultimapValue<K, V>
		extends ObservableObjectValue<ObservableSetMultimap<K, V>>,
		ObservableSetMultimap<K, V> {

}
