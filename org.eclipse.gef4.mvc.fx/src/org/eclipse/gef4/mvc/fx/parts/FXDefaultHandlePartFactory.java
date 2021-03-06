/*******************************************************************************
 * Copyright (c) 2014, 2015 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.fx.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.gef4.common.adapt.AdapterKey;
import org.eclipse.gef4.fx.utils.NodeUtils;
import org.eclipse.gef4.geometry.planar.BezierCurve;
import org.eclipse.gef4.geometry.planar.ICurve;
import org.eclipse.gef4.geometry.planar.IGeometry;
import org.eclipse.gef4.geometry.planar.IShape;
import org.eclipse.gef4.geometry.planar.Rectangle;
import org.eclipse.gef4.mvc.behaviors.HoverBehavior;
import org.eclipse.gef4.mvc.behaviors.IBehavior;
import org.eclipse.gef4.mvc.behaviors.SelectionBehavior;
import org.eclipse.gef4.mvc.parts.IHandlePart;
import org.eclipse.gef4.mvc.parts.IHandlePartFactory;
import org.eclipse.gef4.mvc.parts.IVisualPart;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

import javafx.scene.Node;

/**
 * The {@link FXDefaultHandlePartFactory} is an {@link IHandlePartFactory}
 * implementation that is parameterized by {@link Node}.
 *
 * @author mwienand
 *
 */
public class FXDefaultHandlePartFactory implements IHandlePartFactory<Node> {

	/**
	 * The role name for the <code>Provider&lt;IGeometry&gt;</code> that will be
	 * used to generate selection handles.
	 */
	public static final String SELECTION_HANDLES_GEOMETRY_PROVIDER = "SELECTION_HANDLES_GEOMETRY_PROVIDER";

	/**
	 * The role name for the <code>Provider&lt;IGeometry&gt;</code> that will be
	 * used to generate hover handles.
	 */
	public static final String HOVER_HANDLES_GEOMETRY_PROVIDER = "HOVER_HANDLES_GEOMETRY_PROVIDER";

	@Inject
	private Injector injector;

	/**
	 * Creates an {@link FXRectangleSegmentHandlePart} for one corner of the
	 * bounds of a multi selection. The selection bounds geometry can be
	 * obtained from the given provider as a poly-bezier, i.e. an array of
	 * {@link BezierCurve}s. The position of the handle part to create is
	 * represented via segment index and parameter, where the index refers to
	 * the respective {@link BezierCurve} segment, and the parameter (0 = start,
	 * 0.5 = mid, 1 = end) to the relative position w.r.t. to the indexed
	 * segment.
	 *
	 * @param targets
	 *            The selected {@link IVisualPart}s that form the multi
	 *            selection, for which feedback handles are to be created.
	 * @param contextMap
	 *            May contain additional information needed for the creation or
	 *            to identify the creation context, when querying back such
	 *            information from the context {@link IBehavior} that initiated
	 *            the creation.
	 * @param segmentsProvider
	 *            A provider that delivers the selection geometry as a
	 *            poly-bezier, represented as an array of {@link BezierCurve}s.
	 * @param segmentIndex
	 *            The index of the poly-bezier segment at which the handle is to
	 *            be placed.
	 * @param segmentParameter
	 *            The parameter that identifies the position within the segment
	 *            (0 = start, 0.5 = mid, 1 = end).
	 * @return an {@link IHandlePart} for the specified corner of the bounds of
	 *         the multi selection
	 */
	// TODO: if we pass in the contextMap here, we should also pass in the
	// contextBehavior, because otherwise a back-query scenario cannot be
	// realized.
	protected IHandlePart<Node, ? extends Node> createBoundsSelectionCornerHandlePart(
			final List<? extends IVisualPart<Node, ? extends Node>> targets,
			Map<Object, Object> contextMap,
			Provider<BezierCurve[]> segmentsProvider, int segmentIndex,
			double segmentParameter) {
		FXRectangleSegmentHandlePart part = injector
				.getInstance(FXRectangleSegmentHandlePart.class);
		part.setSegmentsProvider(segmentsProvider);
		part.setSegmentIndex(segmentIndex);
		part.setSegmentParameter(segmentParameter);
		return part;
	}

	/**
	 * Creates handle parts for a multi selection. The handle parts will be
	 * located in the corners of the selection bounds.
	 *
	 * @param targets
	 *            A list containing the {@link IVisualPart}s that are part of
	 *            the multi selection.
	 * @param handleGeometryProvider
	 *            The <code>Provider&lt;IGeometry&gt;</code> that provides the
	 *            selection bounds geometry.
	 * @param contextMap
	 *            A map in which the state-less {@link SelectionBehavior} may
	 *            place additional context information for the creation process.
	 *            It may either directly contain additional information needed
	 *            by this factory, or may be passed back by the factory to the
	 *            calling {@link SelectionBehavior} to query such kind of
	 *            information (in which case it will allow the
	 *            {@link SelectionBehavior} to identify the creation context).
	 * @return A list containing the created handle parts.
	 */
	// TODO: Maybe inline this method
	// TODO: if we pass in the contextMap here, we should also pass in the
	// contextBehavior, because otherwise a back-query scenario cannot be
	// realized.
	protected List<IHandlePart<Node, ? extends Node>> createBoundsSelectionHandleParts(
			final List<? extends IVisualPart<Node, ? extends Node>> targets,
			Provider<? extends IGeometry> handleGeometryProvider,
			Map<Object, Object> contextMap) {
		List<IHandlePart<Node, ? extends Node>> handleParts = new ArrayList<>();

		// per default, handle parts are created for the 4 corners of the
		// multi selection bounds
		Provider<BezierCurve[]> segmentsProvider = createSegmentsProvider(
				handleGeometryProvider);
		for (int i = 0; i < 4; i++) {
			IHandlePart<Node, ? extends Node> part = createBoundsSelectionCornerHandlePart(
					targets, contextMap, segmentsProvider, i, 0);
			if (part != null) {
				handleParts.add(part);
			}
		}
		return handleParts;
	}

	/**
	 * Creates an {@link IHandlePart} for the specified segment vertex of the
	 * {@link IGeometry} provided by the given <i>handleGeometryProvider</i>.
	 *
	 * @param targetPart
	 *            The {@link IVisualPart} which is selected.
	 * @param segmentsProvider
	 *            Provides {@link BezierCurve}s from which the handle part can
	 *            retrieve its location.
	 * @param segmentCount
	 *            The number of segments that exist
	 * @param segmentIndex
	 *            Index of the segment of the provided {@link BezierCurve}s
	 *            where the handle part will be located.
	 * @param segmentParameter
	 *            Parameter between 0 and 1 that specifies the location on the
	 *            segment.
	 * @return {@link IHandlePart} for the specified segment vertex of the
	 *         provided {@link BezierCurve}s
	 */
	// TODO: if we pass in context behavior and context map to the other
	// creation methods, we should also do this here.
	// TODO: remove the segment count parameter here
	protected IHandlePart<Node, ? extends Node> createCurveSelectionHandlePart(
			final IVisualPart<Node, ? extends Node> targetPart,
			Provider<BezierCurve[]> segmentsProvider, int segmentCount,
			int segmentIndex, double segmentParameter) {
		FXCircleSegmentHandlePart part = injector
				.getInstance(FXCircleSegmentHandlePart.class);
		part.setSegmentsProvider(segmentsProvider);
		part.setSegmentIndex(segmentIndex);
		part.setSegmentParameter(segmentParameter);
		return part;
	}

	/**
	 * Generate handles for the end/join points of the individual beziers.
	 *
	 * @param targetPart
	 *            The {@link IVisualPart} which is selected.
	 * @param segmentsProvider
	 *            Provides an {@link IGeometry} for which {@link IHandlePart}s
	 *            are to be created.
	 * @param contextMap
	 *            Stores context information as an {@link IBehavior} is
	 *            stateless.
	 * @return {@link IHandlePart}s for the given target part.
	 */
	// TODO: Maybe inline this method
	// TODO: if we pass in the contextMap here, we should also pass in the
	// contextBehavior, because otherwise a back-query scenario cannot be
	// realized.
	protected List<IHandlePart<Node, ? extends Node>> createCurveSelectionHandleParts(
			final IVisualPart<Node, ? extends Node> targetPart,
			Provider<BezierCurve[]> segmentsProvider,
			Map<Object, Object> contextMap) {
		List<IHandlePart<Node, ? extends Node>> hps = new ArrayList<>();
		BezierCurve[] segments = segmentsProvider.get();
		for (int i = 0; i < segments.length; i++) {
			IHandlePart<Node, ? extends Node> part = createCurveSelectionHandlePart(
					targetPart, segmentsProvider, segments.length, i, 0.0);
			if (part != null) {
				hps.add(part);
			}
			part = createCurveSelectionHandlePart(targetPart, segmentsProvider,
					segments.length, i, 0.5);
			if (part != null) {
				hps.add(part);
			}

			// create handle part for the curve's end point, too
			if (i == segments.length - 1) {
				part = createCurveSelectionHandlePart(targetPart,
						segmentsProvider, segments.length, i, 1.0);
				if (part != null) {
					hps.add(part);
				}
			}
		}
		return hps;
	}

	// entry point
	@Override
	public List<IHandlePart<Node, ? extends Node>> createHandleParts(
			List<? extends IVisualPart<Node, ? extends Node>> targets,
			IBehavior<Node> contextBehavior, Map<Object, Object> contextMap) {
		// no targets
		if (targets == null || targets.isEmpty()) {
			return Collections.emptyList();
		}

		// differentiate creation context
		if (contextBehavior instanceof SelectionBehavior) {
			return createSelectionHandleParts(targets,
					(SelectionBehavior<Node>) contextBehavior, contextMap);
		} else if (contextBehavior instanceof HoverBehavior) {
			// only one part hovered at a time
			if (targets.size() > 1) {
				throw new IllegalStateException(
						"Cannot create hover handles for more than one target.");
			}
			return createHoverHandleParts(targets.get(0),
					(HoverBehavior<Node>) contextBehavior, contextMap);
		}

		// unknown creation context, do not create handles
		return Collections.emptyList();
	}

	/**
	 * Creates hover handle parts for the given (hovered) <i>target</i>
	 * {@link IVisualPart}.
	 *
	 * @param target
	 *            The (hovered) target {@link IVisualPart} for which hover
	 *            handles are created.
	 * @param contextBehavior
	 *            The {@link HoverBehavior} that initiated the creation process.
	 * @param contextMap
	 *            A map in which the state-less {@link HoverBehavior} may place
	 *            additional context information for the creation process. It
	 *            may either directly contain additional information needed by
	 *            this factory, or may be passed back by the factory to the
	 *            calling {@link HoverBehavior} to query such kind of
	 *            information (in which case it will allow the
	 *            {@link HoverBehavior} to identify the creation context).
	 * @return A list containing the created hover handle parts.
	 */
	@SuppressWarnings("serial")
	protected List<IHandlePart<Node, ? extends Node>> createHoverHandleParts(
			final IVisualPart<Node, ? extends Node> target,
			final HoverBehavior<Node> contextBehavior,
			final Map<Object, Object> contextMap) {
		List<IHandlePart<Node, ? extends Node>> handleParts = new ArrayList<>();

		// handle geometry is in target visual local coordinate space.
		final Provider<? extends IGeometry> hoverHandlesGeometryInTargetLocalProvider = target
				.getAdapter(AdapterKey
						.get(new TypeToken<Provider<? extends IGeometry>>() {
						}, HOVER_HANDLES_GEOMETRY_PROVIDER));

		// generate handles from selection handles geometry
		IGeometry hoverHandlesGeometry = (hoverHandlesGeometryInTargetLocalProvider != null)
				? hoverHandlesGeometryInTargetLocalProvider.get() : null;
		if (hoverHandlesGeometry == null) {
			return handleParts; // empty
		}

		// we will need a provider that returns the geometry in scene
		// coordinates
		final Provider<? extends IGeometry> hoverHandlesGeometryInSceneProvider = new Provider<IGeometry>() {
			@Override
			public IGeometry get() {
				return NodeUtils.localToScene(target.getVisual(),
						hoverHandlesGeometryInTargetLocalProvider.get());
			}
		};

		// the handle parts are located based on the segments of the handle
		// geometry
		Provider<BezierCurve[]> hoverHandlesSegmentsInSceneProvider = new Provider<BezierCurve[]>() {
			@Override
			public BezierCurve[] get() {
				IGeometry handleGeometry = hoverHandlesGeometryInSceneProvider
						.get();
				if (handleGeometry instanceof IShape) {
					List<BezierCurve> segments = new ArrayList<>();
					for (ICurve os : ((IShape) handleGeometry)
							.getOutlineSegments()) {
						segments.addAll(Arrays.asList(os.toBezier()));
					}
					return segments.toArray(new BezierCurve[] {});
				} else if (handleGeometry instanceof ICurve) {
					return ((ICurve) handleGeometry).toBezier();
				} else {
					throw new IllegalStateException(
							"Unable to determine handle position: Expected IShape or ICurve but got: "
									+ handleGeometry);
				}
			}
		};

		// create segment handles (based on outline)
		BezierCurve[] segments = hoverHandlesSegmentsInSceneProvider.get();
		for (int i = 0; i < segments.length; i++) {
			IHandlePart<Node, ? extends Node> hp = createHoverSegmentHandlePart(
					target, hoverHandlesSegmentsInSceneProvider,
					segments.length, i, contextMap);
			if (hp != null) {
				handleParts.add(hp);
			}
		}

		return handleParts;
	}

	/**
	 * Creates an {@link FXCircleSegmentHandlePart} for the given (hovered)
	 * <i>target</i> {@link IVisualPart}. The segments provider and segment
	 * index determine the position of the hover handle.
	 *
	 * @param target
	 *            The (hovered) target {@link IVisualPart}.
	 * @param hoverHandlesSegmentsInSceneProvider
	 *            The <code>Provider&lt;BezierCurve[]&gt;</code> that is used to
	 *            determine the handle's position.
	 * @param segmentCount
	 *            The number of segments returned by the segments provider.
	 * @param segmentIndex
	 *            The segment index on which the created handle part is located.
	 * @param contextMap
	 *            A map in which the state-less {@link HoverBehavior} may place
	 *            additional context information for the creation process. It
	 *            may either directly contain additional information needed by
	 *            this factory, or may be passed back by the factory to the
	 *            calling {@link HoverBehavior} to query such kind of
	 *            information (in which case it will allow the
	 *            {@link HoverBehavior} to identify the creation context).
	 * @return An {@link FXCircleSegmentHandlePart} for the given target at the
	 *         specified position.
	 */
	protected IHandlePart<Node, ? extends Node> createHoverSegmentHandlePart(
			final IVisualPart<Node, ? extends Node> target,
			Provider<BezierCurve[]> hoverHandlesSegmentsInSceneProvider,
			int segmentCount, int segmentIndex,
			Map<Object, Object> contextMap) {
		FXCircleSegmentHandlePart part = injector
				.getInstance(FXCircleSegmentHandlePart.class);
		part.setSegmentsProvider(hoverHandlesSegmentsInSceneProvider);
		part.setSegmentIndex(segmentIndex);
		part.setSegmentParameter(0);
		return part;
	}

	/**
	 * Creates handle parts for a multi selection.
	 *
	 * @param targets
	 *            The list of {@link IVisualPart}s for which handles are
	 *            created.
	 * @param contextMap
	 *            A map in which the state-less {@link SelectionBehavior} may
	 *            place additional context information for the creation process.
	 *            It may either directly contain additional information needed
	 *            by this factory, or may be passed back by the factory to the
	 *            calling {@link SelectionBehavior} to query such kind of
	 *            information (in which case it will allow the
	 *            {@link SelectionBehavior} to identify the creation context).
	 * @return A list containing the created handle parts.
	 */
	protected List<IHandlePart<Node, ? extends Node>> createMultiSelectionHandleParts(
			final List<? extends IVisualPart<Node, ? extends Node>> targets,
			Map<Object, Object> contextMap) {
		Provider<? extends IGeometry> handleGeometryProvider = new Provider<IGeometry>() {
			@Override
			public IGeometry get() {
				// TODO: move code out of FXPartUtils into a geometry provider
				// (move to FX)
				return FXPartUtils.getUnionedVisualBoundsInScene(targets);
			}
		};
		return createBoundsSelectionHandleParts(targets, handleGeometryProvider,
				contextMap);
	}

	private Provider<BezierCurve[]> createSegmentsProvider(
			final Provider<? extends IGeometry> geometryProvider) {
		return new Provider<BezierCurve[]>() {
			@Override
			public BezierCurve[] get() {
				IGeometry geometry = geometryProvider.get();
				if (geometry instanceof IShape) {
					List<BezierCurve> segments = new ArrayList<>();
					for (ICurve os : ((IShape) geometry).getOutlineSegments()) {
						segments.addAll(Arrays.asList(os.toBezier()));
					}
					return segments.toArray(new BezierCurve[] {});
				} else if (geometry instanceof ICurve) {
					return ((ICurve) geometry).toBezier();
				} else {
					throw new IllegalStateException(
							"Unable to deduce segments from geometry: Expected IShape or ICurve but got: "
									+ geometry);
				}
			}
		};
	}

	/**
	 * Creates handle parts for a selection.
	 *
	 * @param targets
	 *            The list of {@link IVisualPart}s for which handles are
	 *            created.
	 * @param selectionBehavior
	 *            The {@link SelectionBehavior} that initiated the creation
	 *            process.
	 * @param contextMap
	 *            A map in which the state-less {@link SelectionBehavior} may
	 *            place additional context information for the creation process.
	 *            It may either directly contain additional information needed
	 *            by this factory, or may be passed back by the factory to the
	 *            calling {@link SelectionBehavior} to query such kind of
	 *            information (in which case it will allow the
	 *            {@link SelectionBehavior} to identify the creation context).
	 * @return A list containing the created handle parts.
	 */
	protected List<IHandlePart<Node, ? extends Node>> createSelectionHandleParts(
			List<? extends IVisualPart<Node, ? extends Node>> targets,
			SelectionBehavior<Node> selectionBehavior,
			Map<Object, Object> contextMap) {
		if (targets.isEmpty()) {
			return Collections.emptyList();
		} else if (targets.size() == 1) {
			return createSingleSelectionHandleParts(targets.get(0), contextMap);
		} else {
			// multiple selection uses bounds
			return createMultiSelectionHandleParts(targets, contextMap);
		}
	}

	/**
	 * Creates an {@link IHandlePart} for the specified vertex of the
	 * {@link IGeometry} provided by the given <i>handleGeometryProvider</i>.
	 *
	 * @param target
	 *            {@link IVisualPart} for which a selection handle is created.
	 * @param segmentsProvider
	 *            Provides the {@link BezierCurve}s from which the handle can
	 *            retrieve its location.
	 * @param segmentCount
	 *            Number of segments.
	 * @param segmentIndex
	 *            Index of the segment where the handle is located.
	 * @param contextMap
	 *            Stores context information as an {@link IBehavior} is
	 *            stateless.
	 * @return {@link IHandlePart} for the specified vertex of the
	 *         {@link IGeometry} provided by the <i>handleGeometryProvider</i>
	 */
	protected IHandlePart<Node, ? extends Node> createSelectionSegmentHandlePart(
			final IVisualPart<Node, ? extends Node> target,
			Provider<BezierCurve[]> segmentsProvider, int segmentCount,
			int segmentIndex, Map<Object, Object> contextMap) {
		FXCircleSegmentHandlePart part = injector
				.getInstance(FXCircleSegmentHandlePart.class);
		part.setSegmentsProvider(segmentsProvider);
		part.setSegmentIndex(segmentIndex);
		part.setSegmentParameter(0);
		injector.injectMembers(part);
		return part;
	}

	/**
	 * Creates handle parts for a single selection.
	 *
	 * @param target
	 *            The {@link IVisualPart} for which handles are created.
	 * @param contextMap
	 *            A map in which the state-less {@link SelectionBehavior} may
	 *            place additional context information for the creation process.
	 *            It may either directly contain additional information needed
	 *            by this factory, or may be passed back by the factory to the
	 *            calling {@link SelectionBehavior} to query such kind of
	 *            information (in which case it will allow the
	 *            {@link SelectionBehavior} to identify the creation context).
	 * @return A list containing the created handle parts.
	 */
	// TODO: Add parameter for SelectionBehavior so that back queries are
	// possible
	@SuppressWarnings("serial")
	protected List<IHandlePart<Node, ? extends Node>> createSingleSelectionHandleParts(
			final IVisualPart<Node, ? extends Node> target,
			Map<Object, Object> contextMap) {
		List<IHandlePart<Node, ? extends Node>> handleParts = new ArrayList<>();

		// handle geometry is in target visual local coordinate space.
		final Provider<? extends IGeometry> selectionHandlesGeometryInTargetLocalProvider = target
				.getAdapter(AdapterKey
						.get(new TypeToken<Provider<? extends IGeometry>>() {
						}, SELECTION_HANDLES_GEOMETRY_PROVIDER));

		// generate handles from selection handles geometry
		IGeometry selectionHandlesGeometry = (selectionHandlesGeometryInTargetLocalProvider != null)
				? selectionHandlesGeometryInTargetLocalProvider.get() : null;

		if (selectionHandlesGeometry == null) {
			return handleParts; // empty
		}

		// we will need a provider that returns the geometry in scene
		// coordinates
		final Provider<IGeometry> selectionHandlesGeometryInSceneProvider = new Provider<IGeometry>() {
			@Override
			public IGeometry get() {
				return NodeUtils.localToScene(target.getVisual(),
						selectionHandlesGeometryInTargetLocalProvider.get());
			}
		};
		Provider<BezierCurve[]> selectionHandlesSegmentsInSceneProvider = createSegmentsProvider(
				selectionHandlesGeometryInSceneProvider);

		if (selectionHandlesGeometry instanceof ICurve) {
			// assure the geometry provider that is handed over returns the
			// geometry in scene coordinates
			handleParts.addAll(createCurveSelectionHandleParts(target,
					selectionHandlesSegmentsInSceneProvider, contextMap));
		} else if (selectionHandlesGeometry instanceof IShape) {
			if (selectionHandlesGeometry instanceof Rectangle) {
				// create corner handles
				handleParts.addAll(createTightBoundsSelectionHandleParts(
						Collections.singletonList(target),
						selectionHandlesSegmentsInSceneProvider, contextMap));
			} else {
				// create segment handles (based on outline)
				BezierCurve[] segments = selectionHandlesSegmentsInSceneProvider
						.get();
				for (int i = 0; i < segments.length; i++) {
					IHandlePart<Node, ? extends Node> hp = createSelectionSegmentHandlePart(
							target, selectionHandlesSegmentsInSceneProvider,
							segments.length, i, contextMap);
					if (hp != null) {
						handleParts.add(hp);
					}
				}
			}
		} else {
			throw new IllegalStateException(
					"Unable to generate handles for this handle geometry. Expected ICurve or IShape, but got: "
							+ selectionHandlesGeometry);
		}
		return handleParts;
	}

	/**
	 * Creates {@link FXRectangleSegmentHandlePart}s for the segments provided
	 * by the given segments provider.
	 *
	 * @param targetParts
	 *            A list containing the {@link IVisualPart}s for which handles
	 *            are created.
	 * @param segmentsProvider
	 *            The <code>Provider&lt;BezierCurve[]&gt;</code> that is used to
	 *            determine the handles's positions.
	 * @param contextMap
	 *            A map in which the state-less {@link SelectionBehavior} may
	 *            place additional context information for the creation process.
	 *            It may either directly contain additional information needed
	 *            by this factory, or may be passed back by the factory to the
	 *            calling {@link SelectionBehavior} to query such kind of
	 *            information (in which case it will allow the
	 *            {@link SelectionBehavior} to identify the creation context).
	 * @return A list containing the created handle parts.
	 */
	// TODO: Remove parameter targetParts since they are not used (maybe pass in
	// the single target part)
	// TODO: Add parameter for SelectionBehavior so that back queries are
	// possible
	protected Collection<? extends IHandlePart<Node, ? extends Node>> createTightBoundsSelectionHandleParts(
			List<? extends IVisualPart<Node, ? extends Node>> targetParts,
			Provider<BezierCurve[]> segmentsProvider,
			Map<Object, Object> contextMap) {
		List<IHandlePart<Node, ? extends Node>> hps = new ArrayList<>();
		BezierCurve[] segments = segmentsProvider.get();
		for (int i = 0; i < segments.length; i++) {
			FXRectangleSegmentHandlePart part = injector
					.getInstance(FXRectangleSegmentHandlePart.class);
			part.setSegmentsProvider(segmentsProvider);
			part.setSegmentIndex(i);
			part.setSegmentParameter(0);
			hps.add(part);
		}
		return hps;
	}

}
