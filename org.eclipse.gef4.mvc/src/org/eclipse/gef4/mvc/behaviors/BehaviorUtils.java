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
package org.eclipse.gef4.mvc.behaviors;

import java.util.List;

import org.eclipse.gef4.mvc.parts.IFeedbackPart;
import org.eclipse.gef4.mvc.parts.IHandlePart;
import org.eclipse.gef4.mvc.parts.IRootPart;
import org.eclipse.gef4.mvc.parts.IVisualPart;

/**
 * The {@link BehaviorUtils} class provides utility methods for the
 * implementation of {@link IBehavior}s, such as the creation of
 * {@link IFeedbackPart}s and {@link IHandlePart}s, or the
 * establishment/unestablishment of anchor relations.
 */
// TODO: Transfer this into a utility class that can be injected (and thus
// replaced) in the parts/policies where its needed, providing non-static
// functions.
public class BehaviorUtils {

	/**
	 * Adds the given list of anchoreds as children to the given
	 * {@link IRootPart}. Additionally, the anchoreds are attached to the given
	 * anchorages.
	 *
	 * @param root
	 *            The {@link IRootPart} to which the anchoreds are added as
	 *            children.
	 * @param anchorages
	 *            The {@link IVisualPart}s to which the given anchoreds are
	 *            attached.
	 * @param anchoreds
	 *            The {@link IVisualPart}s that are added as children to the
	 *            given {@link IRootPart} and attached to the given anchorage.
	 * @param <VR>
	 *            The visual root node of the UI toolkit this
	 *            {@link IVisualPart} is used in, e.g. javafx.scene.Node in case
	 *            of JavaFX.
	 * @see #removeAnchoreds(IRootPart, List, List)
	 */
	public static <VR> void addAnchoreds(IRootPart<VR, ? extends VR> root,
			List<? extends IVisualPart<VR, ? extends VR>> anchorages,
			List<? extends IVisualPart<VR, ? extends VR>> anchoreds) {
		if (anchoreds != null && !anchoreds.isEmpty()) {
			root.addChildren(anchoreds);
			for (IVisualPart<VR, ? extends VR> anchored : anchoreds) {
				for (IVisualPart<VR, ? extends VR> anchorage : anchorages) {
					anchored.attachToAnchorage(anchorage);
				}
			}
		}
	}

	/**
	 * Removes the given list of anchoreds as children from the given
	 * {@link IRootPart}. Additionally detaches them from the given anchorages.
	 *
	 * @param root
	 *            The {@link IRootPart} from which the anchoreds are removed.
	 * @param anchorages
	 *            The anchorages from which the given anchoreds are detached.
	 * @param anchoreds
	 *            The anchoreds that are removed and detached.
	 * @param <VR>
	 *            The visual root node of the UI toolkit this
	 *            {@link IVisualPart} is used in, e.g. javafx.scene.Node in case
	 *            of JavaFX.
	 * @see #addAnchoreds(IRootPart, List, List)
	 */
	public static <VR> void removeAnchoreds(IRootPart<VR, ? extends VR> root,
			List<? extends IVisualPart<VR, ? extends VR>> anchorages,
			List<? extends IVisualPart<VR, ? extends VR>> anchoreds) {
		if (anchoreds != null && !anchoreds.isEmpty()) {
			root.removeChildren(anchoreds);
			for (IVisualPart<VR, ? extends VR> anchored : anchoreds) {
				for (IVisualPart<VR, ? extends VR> anchorage : anchorages) {
					anchored.detachFromAnchorage(anchorage);
				}
			}
		}
	}

}
