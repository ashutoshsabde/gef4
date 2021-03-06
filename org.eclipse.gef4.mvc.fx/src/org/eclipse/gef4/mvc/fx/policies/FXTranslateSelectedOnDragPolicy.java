/*******************************************************************************
 * Copyright (c) 2014, 2015 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.fx.policies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef4.geometry.planar.Dimension;
import org.eclipse.gef4.geometry.planar.Point;
import org.eclipse.gef4.mvc.models.SelectionModel;
import org.eclipse.gef4.mvc.parts.IContentPart;

import com.google.common.reflect.TypeToken;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * The {@link FXTranslateSelectedOnDragPolicy} is an
 * {@link AbstractFXOnDragPolicy} that relocates its {@link #getHost() host}
 * when it is dragged with the mouse.
 *
 * @author anyssen
 *
 */
public class FXTranslateSelectedOnDragPolicy extends AbstractFXOnDragPolicy {

	private Point initialMouseLocationInScene = null;
	private Map<IContentPart<Node, ? extends Node>, Integer> translationIndices = new HashMap<>();
	private List<IContentPart<Node, ? extends Node>> targetParts;

	@Override
	public void drag(MouseEvent e, Dimension delta) {
		// abort this policy if no target parts could be found
		if (targetParts == null) {
			return;
		}

		// apply changes to the target parts
		for (IContentPart<Node, ? extends Node> part : targetParts) {
			FXTransformPolicy policy = getTransformPolicy(part);
			if (policy != null) {
				Point2D startInParent = getHost().getVisual().getParent()
						.sceneToLocal(0, 0);
				Point2D endInParent = getHost().getVisual().getParent()
						.sceneToLocal(delta.width, delta.height);
				Point2D deltaInParent = new Point2D(
						endInParent.getX() - startInParent.getX(),
						endInParent.getY() - startInParent.getY());
				// TODO: snap to grid
				policy.setPostTranslate(translationIndices.get(part),
						deltaInParent.getX(), deltaInParent.getY());
			}
		}
	}

	/**
	 * Returns the initial mouse location in scene coordinates.
	 *
	 * @return The initial mouse location in scene coordinates.
	 */
	protected Point getInitialMouseLocationInScene() {
		return initialMouseLocationInScene;
	}

	/**
	 * Returns a {@link List} containing all {@link IContentPart}s that should
	 * be relocated by this policy.
	 *
	 * @return A {@link List} containing all {@link IContentPart}s that should
	 *         be relocated by this policy.
	 */
	// TODO: change visibility to protected
	@SuppressWarnings("serial")
	public List<IContentPart<Node, ? extends Node>> getTargetParts() {
		return getHost().getRoot().getViewer()
				.getAdapter(new TypeToken<SelectionModel<Node>>() {
				}).getSelection();
	}

	/**
	 * Returns the {@link FXTransformPolicy} that is installed on the given
	 * {@link IContentPart}.
	 *
	 * @param part
	 *            The {@link IContentPart} for which to return the installed
	 *            {@link FXTransformPolicy}.
	 * @return The {@link FXTransformPolicy} that is installed on the given
	 *         {@link IContentPart}.
	 */
	protected FXTransformPolicy getTransformPolicy(
			IContentPart<Node, ? extends Node> part) {
		return part.getAdapter(FXTransformPolicy.class);
	}

	// @Override
	// public boolean isExclusive() {
	// return true;
	// }

	@Override
	public void press(MouseEvent e) {
		// save initial pointer location
		setInitialMouseLocationInScene(new Point(e.getSceneX(), e.getSceneY()));

		// determine target parts
		targetParts = getTargetParts();
		if (targetParts.isEmpty()) {
			// abort this policy if no target parts could be found
			targetParts = null;
			return;
		}

		// initialize this policy for all determined target parts
		for (IContentPart<Node, ? extends Node> part : targetParts) {
			// init transaction policy
			FXTransformPolicy policy = getTransformPolicy(part);
			if (policy != null) {
				storeAndDisableRefreshVisuals(part);
				init(policy);
				translationIndices.put(part, policy.createPostTransform());
			}
		}
	}

	@Override
	public void release(MouseEvent e, Dimension delta) {
		// abort this policy if no target parts could be found
		if (targetParts == null) {
			return;
		}

		// commit changes for all target parts
		for (IContentPart<Node, ? extends Node> part : targetParts) {
			FXTransformPolicy policy = getTransformPolicy(part);
			if (policy != null) {
				commit(policy);
				restoreRefreshVisuals(part);
			}
		}

		// reset target parts
		targetParts = null;
		// reset initial pointer location
		setInitialMouseLocationInScene(null);
		// reset translation indices
		translationIndices.clear();
	}

	/**
	 * Sets the initial mouse location to the given value.
	 *
	 * @param point
	 *            The initial mouse location.
	 */
	protected void setInitialMouseLocationInScene(Point point) {
		initialMouseLocationInScene = point;
	}

}
