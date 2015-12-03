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
 * Note: Parts of this class have been transferred from org.eclipse.gef.editpolicies.AbstractEditPolicy.
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.behaviors;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef4.common.activate.IActivatable;
import org.eclipse.gef4.common.inject.AdaptableScopes;
import org.eclipse.gef4.mvc.domain.IDomain;
import org.eclipse.gef4.mvc.parts.IFeedbackPart;
import org.eclipse.gef4.mvc.parts.IFeedbackPartFactory;
import org.eclipse.gef4.mvc.parts.IHandlePart;
import org.eclipse.gef4.mvc.parts.IHandlePartFactory;
import org.eclipse.gef4.mvc.parts.IVisualPart;
import org.eclipse.gef4.mvc.viewer.IViewer;

import com.google.inject.Inject;

/**
 *
 * @author anyssen
 *
 * @param <VR>
 *            The visual root node of the UI toolkit used, e.g.
 *            javafx.scene.Node in case of JavaFX.
 */
public abstract class AbstractBehavior<VR> implements IBehavior<VR> {

	/**
	 * A {@link PropertyChangeSupport} that is used as a delegate to notify
	 * listeners about changes to this object. May be used by subclasses to
	 * trigger the notification of listeners.
	 */
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	@Inject
	// scoped to single instance within viewer
	private IFeedbackPartFactory<VR> feedbackPartFactory;

	@Inject
	// scoped to single instance within viewer
	private IHandlePartFactory<VR> handlePartFactory;
	private IVisualPart<VR, ? extends VR> host;
	private boolean active;

	private Map<IVisualPart<VR, ? extends VR>, List<IHandlePart<VR, ? extends VR>>> handleParts = new HashMap<IVisualPart<VR, ? extends VR>, List<IHandlePart<VR, ? extends VR>>>();
	private Map<IVisualPart<VR, ? extends VR>, List<IFeedbackPart<VR, ? extends VR>>> feedbackParts = new HashMap<IVisualPart<VR, ? extends VR>, List<IFeedbackPart<VR, ? extends VR>>>();

	@Override
	public void activate() {
		boolean oldActive = active;
		active = true;
		if (oldActive != active) {
			pcs.firePropertyChange(IActivatable.ACTIVE_PROPERTY, oldActive,
					active);
		}
	}

	/**
	 * Switches to the relevant adaptable scopes (
	 * {@link #switchAdaptableScopes()}) and uses the injected
	 * {@link IFeedbackPartFactory} to create feedback parts for the given list
	 * of target parts. The resulting feedback parts are anchored to the target
	 * parts and added as children to the {@link #getHost() host's} root part.
	 *
	 * @see #addFeedback(List, Map)
	 * @param targets
	 *            The {@link IVisualPart}s for which feedback parts should be
	 *            generated.
	 */
	protected void addFeedback(
			List<? extends IVisualPart<VR, ? extends VR>> targets) {
		addFeedback(targets, Collections.<Object, Object> emptyMap());
	}

	/**
	 * Switches to the relevant adaptable scopes (
	 * {@link #switchAdaptableScopes()}) and uses the injected
	 * {@link IFeedbackPartFactory} to create feedback parts for the given list
	 * of target parts and the given context map. The resulting feedback parts
	 * are anchored to the target parts and added as children to the
	 * {@link #getHost() host's} root part.
	 *
	 * @param targets
	 *            The {@link IVisualPart}s for which feedback parts should be
	 *            generated.
	 * @param contextMap
	 *            A map containing context information for the creation of the
	 *            feedback parts.
	 */
	protected void addFeedback(
			List<? extends IVisualPart<VR, ? extends VR>> targets,
			Map<Object, Object> contextMap) {
		if (targets != null && !targets.isEmpty()) {
			// create feedback parts, adjusting the relevant adapter scopes
			// before
			switchAdaptableScopes();

			// iterate over targets and generate feedback parts per target
			for (IVisualPart<VR, ? extends VR> target : targets) {
				if (feedbackParts.containsKey(target)) {
					throw new IllegalStateException(
							"Attempt to add feedback twice for the same target <"
									+ target + ">.");
				}
				List<IFeedbackPart<VR, ? extends VR>> targetFeedbackParts = feedbackPartFactory
						.createFeedbackParts(Collections.singletonList(target),
								this, contextMap);
				feedbackParts.put(target, targetFeedbackParts);
				BehaviorUtils.<VR> addAnchoreds(getHost().getRoot(),
						Collections.singletonList(target), targetFeedbackParts);
			}
		}
	}

	/**
	 * Switches to the relevant adaptable scopes (
	 * {@link #switchAdaptableScopes()}) and uses the injected
	 * {@link IHandlePartFactory} to create handle parts for the given list of
	 * target parts. The resulting handle parts are anchored to the target parts
	 * and added as children to the {@link #getHost() host's} root part.
	 *
	 * @see #addHandles(List, Map)
	 * @param targets
	 *            The {@link IVisualPart}s for which handle parts should be
	 *            generated.
	 */
	protected void addHandles(
			List<? extends IVisualPart<VR, ? extends VR>> targets) {
		addHandles(targets, Collections.<Object, Object> emptyMap());
	}

	/**
	 * Switches to the relevant adaptable scopes (
	 * {@link #switchAdaptableScopes()}) and uses the injected
	 * {@link IHandlePartFactory} to create handle parts for the given list of
	 * target parts and the given context map. The resulting handle parts are
	 * anchored to the target parts and added as children to the
	 * {@link #getHost() host's} root part.
	 *
	 * @param targets
	 *            The {@link IVisualPart}s for which handle parts should be
	 *            generated.
	 * @param contextMap
	 *            A map containing context information for the creation of the
	 *            handle parts.
	 */
	protected void addHandles(
			List<? extends IVisualPart<VR, ? extends VR>> targets,
			Map<Object, Object> contextMap) {
		if (targets != null && !targets.isEmpty()) {
			// create handle parts, adjusting the relevant adaptable scopes
			// before
			switchAdaptableScopes();

			if (targets.size() > 1) {
				// generate multi target handles
				List<IHandlePart<VR, ? extends VR>> targetHandleParts = handlePartFactory
						.createHandleParts(targets, this, contextMap);
				handleParts.put(getHost().getRoot(), targetHandleParts);
				BehaviorUtils.<VR> addAnchoreds(getHost().getRoot(), targets,
						targetHandleParts);
			} else {
				// generate single target handles
				IVisualPart<VR, ? extends VR> target = targets.get(0);
				if (handleParts.containsKey(target)) {
					throw new IllegalStateException(
							"Attempt to add handles twice for the same target <"
									+ target + ">.");
				}
				List<IHandlePart<VR, ? extends VR>> targetHandleParts = handlePartFactory
						.createHandleParts(Collections.singletonList(target),
								this, contextMap);
				handleParts.put(target, targetHandleParts);
				BehaviorUtils.<VR> addAnchoreds(getHost().getRoot(),
						Collections.singletonList(target), targetHandleParts);
			}
		}
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void deactivate() {
		boolean oldActive = active;
		active = false;
		if (oldActive != active) {
			pcs.firePropertyChange(IActivatable.ACTIVE_PROPERTY, oldActive,
					active);
		}
	}

	@Override
	public IVisualPart<VR, ? extends VR> getAdaptable() {
		return getHost();
	}

	/**
	 * Returns a map containing the feedback parts most recently created by this
	 * behavior.
	 *
	 * @return A map containing the feedback parts most recently created by this
	 *         behavior.
	 */
	protected Map<IVisualPart<VR, ? extends VR>, List<IFeedbackPart<VR, ? extends VR>>> getFeedbackParts() {
		return feedbackParts;
	}

	/**
	 * Returns a map containing the handle parts most recently created by this
	 * behavior.
	 *
	 * @return A map containing the handle parts most recently created by this
	 *         behavior.
	 */
	protected Map<IVisualPart<VR, ? extends VR>, List<IHandlePart<VR, ? extends VR>>> getHandleParts() {
		return handleParts;
	}

	@Override
	public IVisualPart<VR, ? extends VR> getHost() {
		return host;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	/**
	 * Removes the feedback parts previously created for the given target parts.
	 *
	 * @param targets
	 *            The list of target parts for which previously created feedback
	 *            is to be removed.
	 */
	protected void removeFeedback(
			List<? extends IVisualPart<VR, ? extends VR>> targets) {
		if (feedbackParts != null && !feedbackParts.isEmpty()) {
			if (targets != null && !targets.isEmpty()) {
				for (IVisualPart<VR, ? extends VR> target : targets) {
					BehaviorUtils.<VR> removeAnchoreds(getHost().getRoot(),
							Collections.singletonList(target),
							feedbackParts.get(target));
					feedbackParts.remove(target);
				}
			}
		}
	}

	/**
	 * Removes the handle parts previously created for the given target parts.
	 *
	 * @param targets
	 *            The list of target parts for which previously created handles
	 *            are to be removed.
	 */
	protected void removeHandles(
			List<? extends IVisualPart<VR, ? extends VR>> targets) {
		if (handleParts != null && !handleParts.isEmpty()) {
			if (targets != null && !targets.isEmpty()) {
				if (targets.size() > 1) {
					// remove multi target handles
					BehaviorUtils.<VR> removeAnchoreds(getHost().getRoot(),
							targets, handleParts.get(getHost().getRoot()));
					handleParts.remove(getHost().getRoot());
				} else {
					// remove single target handles
					IVisualPart<VR, ? extends VR> target = targets.get(0);
					BehaviorUtils.<VR> removeAnchoreds(getHost().getRoot(),
							Collections.singletonList(target),
							handleParts.get(target));
					handleParts.remove(target);
				}
			}
		}
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	@Override
	public void setAdaptable(IVisualPart<VR, ? extends VR> adaptable) {
		this.host = adaptable;
	}

	/**
	 * Adjusts the relevant adaptable scopes to refer to the host of this
	 * behavior, it's viewer, and it's domain, respectively.
	 */
	protected void switchAdaptableScopes() {
		// adjust relevant adaptable scopes before creating new part
		// TODO: move this into AdaptableScopes, making it more generic (i.e.
		// traverse adaptables)
		IVisualPart<VR, ? extends VR> host = getHost();
		IViewer<VR> viewer = host.getRoot().getViewer();
		IDomain<VR> domain = viewer.getDomain();
		AdaptableScopes.switchTo(domain);
		AdaptableScopes.switchTo(viewer);
		AdaptableScopes.switchTo(host);
	}

}