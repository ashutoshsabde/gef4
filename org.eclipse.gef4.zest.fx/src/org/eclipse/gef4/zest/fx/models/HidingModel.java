/*******************************************************************************
 * Copyright (c) 2014, 2015 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API & implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.zest.fx.models;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.gef4.common.properties.IPropertyChangeNotifier;
import org.eclipse.gef4.common.properties.PropertyChangeNotifierSupport;
import org.eclipse.gef4.graph.Node;
import org.eclipse.gef4.mvc.parts.IContentPart;
import org.eclipse.gef4.zest.fx.parts.NodeContentPart;

/**
 * The {@link HidingModel} manages a {@link Set} of currently hidden
 * {@link org.eclipse.gef4.graph.Node}s. The hidden neighbors of a
 * {@link org.eclipse.gef4.graph.Node} can be identified using
 * {@link #getHiddenNeighbors(org.eclipse.gef4.graph.Node)}.
 *
 * @author mwienand
 *
 */
public class HidingModel implements IPropertyChangeNotifier {

	/**
	 * Property name that is used when firing property change notifications when
	 * the {@link Set} of hidden {@link org.eclipse.gef4.graph.Node}s changes.
	 */
	public static final String HIDDEN_PROPERTY = "hidden";

	private final PropertyChangeNotifierSupport pcs = new PropertyChangeNotifierSupport(this);
	private final Set<org.eclipse.gef4.graph.Node> hidden = Collections
			.newSetFromMap(new IdentityHashMap<org.eclipse.gef4.graph.Node, Boolean>());

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	/**
	 * Returns a {@link Set} containing all {@link NodeContentPart}s
	 * corresponding to the hidden neighbors of the content of the given
	 * {@link NodeContentPart}.
	 *
	 * @param nodePart
	 *            The {@link NodeContentPart} of which the hidden neighbors are
	 *            returned.
	 * @return A {@link Set} containing all hidden neighbors of the given
	 *         {@link NodeContentPart}.
	 */
	public Set<NodeContentPart> getHiddenNeighborParts(NodeContentPart nodePart) {
		Set<Node> hiddenNeighbors = getHiddenNeighbors(nodePart.getContent());
		Set<NodeContentPart> hiddenNeighborParts = Collections
				.newSetFromMap(new IdentityHashMap<NodeContentPart, Boolean>());
		Map<Object, IContentPart<javafx.scene.Node, ? extends javafx.scene.Node>> contentPartMap = nodePart.getRoot()
				.getViewer().getContentPartMap();
		for (org.eclipse.gef4.graph.Node neighbor : hiddenNeighbors) {
			hiddenNeighborParts.add((NodeContentPart) contentPartMap.get(neighbor));
		}
		return hiddenNeighborParts;
	}

	/**
	 * Returns a {@link Set} containing all hidden neighbors of the given
	 * {@link org.eclipse.gef4.graph.Node}.
	 *
	 * @param node
	 *            The {@link org.eclipse.gef4.graph.Node} of which the hidden
	 *            neighbors are returned.
	 * @return A {@link Set} containing all hidden neighbors of the given
	 *         {@link org.eclipse.gef4.graph.Node}.
	 */
	public Set<org.eclipse.gef4.graph.Node> getHiddenNeighbors(org.eclipse.gef4.graph.Node node) {
		Set<org.eclipse.gef4.graph.Node> neighbors = node.getLocalNeighbors();
		Set<org.eclipse.gef4.graph.Node> hiddenNeighbors = Collections
				.newSetFromMap(new IdentityHashMap<org.eclipse.gef4.graph.Node, Boolean>());
		for (org.eclipse.gef4.graph.Node neighbor : neighbors) {
			if (isHidden(neighbor)) {
				hiddenNeighbors.add(neighbor);
			}
		}
		return hiddenNeighbors;
	}

	/**
	 * Returns a copy of the {@link Set} that contains all hidden
	 * {@link org.eclipse.gef4.graph.Node}s.
	 *
	 * @return A copy of the {@link Set} that contains all hidden
	 *         {@link org.eclipse.gef4.graph.Node}s.
	 */
	public Set<org.eclipse.gef4.graph.Node> getHiddenNodes() {
		Set<org.eclipse.gef4.graph.Node> copy = Collections
				.newSetFromMap(new IdentityHashMap<org.eclipse.gef4.graph.Node, Boolean>());
		copy.addAll(hidden);
		return copy;
	}

	/**
	 * Returns <code>true</code> if at least one neighbor of the given
	 * {@link NodeContentPart} is currently hidden. Otherwise returns
	 * <code>false</code>.
	 *
	 * @param nodePart
	 *            The {@link NodeContentPart} that is tested for hidden
	 *            neighbors.
	 * @return <code>true</code> if at least one neighbor of the given
	 *         {@link NodeContentPart} is currently hidden, otherwise
	 *         <code>false</code>.
	 */
	public boolean hasHiddenNeighbors(NodeContentPart nodePart) {
		return hasHiddenNeighbors(nodePart.getContent());
	}

	/**
	 * Returns <code>true</code> if at least one neighbor of the given
	 * {@link org.eclipse.gef4.graph.Node} is currently hidden. Otherwise
	 * returns <code>false</code>.
	 *
	 * @param node
	 *            The {@link org.eclipse.gef4.graph.Node} that is tested for
	 *            hidden neighbors.
	 * @return <code>true</code> if at least one neighbor of the given
	 *         {@link org.eclipse.gef4.graph.Node} is currently hidden,
	 *         otherwise <code>false</code>.
	 */
	public boolean hasHiddenNeighbors(org.eclipse.gef4.graph.Node node) {
		return getHiddenNeighbors(node).size() > 0;
	}

	/**
	 * Adds the content of the given {@link NodeContentPart} to the {@link Set}
	 * of hidden {@link org.eclipse.gef4.graph.Node}s. Notifies all property
	 * change listeners about this change.
	 *
	 * @param nodePart
	 *            The {@link NodeContentPart} that is added to the {@link Set}
	 *            of hidden {@link org.eclipse.gef4.graph.Node}s.
	 */
	public void hide(NodeContentPart nodePart) {
		hide(nodePart.getContent());
	}

	/**
	 * Adds the given {@link org.eclipse.gef4.graph.Node} to the {@link Set} of
	 * hidden {@link org.eclipse.gef4.graph.Node}s. Notifies all property change
	 * listeners about this change.
	 *
	 * @param node
	 *            The {@link org.eclipse.gef4.graph.Node} that is added to the
	 *            {@link Set} of hidden {@link org.eclipse.gef4.graph.Node}s.
	 */
	public void hide(org.eclipse.gef4.graph.Node node) {
		Set<org.eclipse.gef4.graph.Node> oldHidden = getHiddenNodes();
		hidden.add(node);
		pcs.firePropertyChange(HIDDEN_PROPERTY, oldHidden, getHiddenNodes());
	}

	/**
	 * Returns <code>true</code> if the given {@link NodeContentPart} is
	 * currently contained within the {@link Set} of hidden
	 * {@link org.eclipse.gef4.graph.Node}s. Otherwise, returns
	 * <code>false</code>.
	 *
	 * @param nodePart
	 *            The {@link NodeContentPart} in question.
	 * @return <code>true</code> if the given
	 *         {@link org.eclipse.gef4.graph.Node} is currently contained within
	 *         the {@link Set} of hidden {@link org.eclipse.gef4.graph.Node}s,
	 *         otherwise <code>false</code>.
	 */
	public boolean isHidden(NodeContentPart nodePart) {
		return isHidden(nodePart.getContent());
	}

	/**
	 * Returns <code>true</code> if the given
	 * {@link org.eclipse.gef4.graph.Node} is currently contained within the
	 * {@link Set} of hidden {@link org.eclipse.gef4.graph.Node}s. Otherwise,
	 * returns <code>false</code>.
	 *
	 * @param node
	 *            The {@link org.eclipse.gef4.graph.Node} in question.
	 * @return <code>true</code> if the content of the given
	 *         {@link NodeContentPart} is currently contained within the
	 *         {@link Set} of hidden {@link org.eclipse.gef4.graph.Node}s,
	 *         otherwise <code>false</code>.
	 */
	public boolean isHidden(org.eclipse.gef4.graph.Node node) {
		return hidden.contains(node);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	/**
	 * Remove the content of the given {@link NodeContentPart} from the
	 * {@link Set} of hidden {@link org.eclipse.gef4.graph.Node} s. Notifies all
	 * property change listeners about this change.
	 *
	 * @param nodePart
	 *            The {@link NodeContentPart} of which the content is removed
	 *            from the {@link Set} of hidden
	 *            {@link org.eclipse.gef4.graph.Node} s.
	 */
	public void show(NodeContentPart nodePart) {
		show(nodePart.getContent());
	}

	/**
	 * Remove the given {@link org.eclipse.gef4.graph.Node} from the {@link Set}
	 * of hidden {@link org.eclipse.gef4.graph.Node} s. Notifies all property
	 * change listeners about this change.
	 *
	 * @param node
	 *            The {@link org.eclipse.gef4.graph.Node} that is removed from
	 *            the {@link Set} of hidden {@link org.eclipse.gef4.graph.Node}
	 *            s.
	 */
	public void show(org.eclipse.gef4.graph.Node node) {
		Set<org.eclipse.gef4.graph.Node> oldHidden = getHiddenNodes();
		hidden.remove(node);
		pcs.firePropertyChange(HIDDEN_PROPERTY, oldHidden, getHiddenNodes());
	}

}
