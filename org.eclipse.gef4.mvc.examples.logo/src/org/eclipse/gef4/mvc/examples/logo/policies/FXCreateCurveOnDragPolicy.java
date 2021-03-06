/*******************************************************************************
 * Copyright (c) 2015 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.examples.logo.policies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.gef4.common.adapt.AdapterKey;
import org.eclipse.gef4.geometry.planar.Dimension;
import org.eclipse.gef4.geometry.planar.Point;
import org.eclipse.gef4.mvc.examples.logo.model.FXGeometricCurve;
import org.eclipse.gef4.mvc.examples.logo.model.FXGeometricModel;
import org.eclipse.gef4.mvc.examples.logo.parts.FXGeometricCurvePart;
import org.eclipse.gef4.mvc.examples.logo.parts.FXGeometricModelPart;
import org.eclipse.gef4.mvc.examples.logo.parts.FXGeometricShapePart;
import org.eclipse.gef4.mvc.fx.parts.FXCircleSegmentHandlePart;
import org.eclipse.gef4.mvc.fx.policies.AbstractFXOnDragPolicy;
import org.eclipse.gef4.mvc.fx.tools.FXClickDragTool;
import org.eclipse.gef4.mvc.fx.viewer.FXViewer;
import org.eclipse.gef4.mvc.models.SelectionModel;
import org.eclipse.gef4.mvc.operations.DeselectOperation;
import org.eclipse.gef4.mvc.operations.ITransactionalOperation;
import org.eclipse.gef4.mvc.parts.IContentPart;
import org.eclipse.gef4.mvc.parts.IVisualPart;
import org.eclipse.gef4.mvc.policies.CreationPolicy;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multiset;
import com.google.common.reflect.TypeToken;

import javafx.event.EventTarget;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class FXCreateCurveOnDragPolicy extends AbstractFXOnDragPolicy {

	private FXCircleSegmentHandlePart bendTargetPart;

	@Override
	public void drag(MouseEvent e, Dimension delta) {
		if (bendTargetPart == null) {
			return;
		}

		// forward drag events to bend target part
		Map<AdapterKey<? extends AbstractFXOnDragPolicy>, AbstractFXOnDragPolicy> dragPolicies = bendTargetPart
				.getAdapters(FXClickDragTool.DRAG_TOOL_POLICY_KEY);
		for (AbstractFXOnDragPolicy dragPolicy : dragPolicies.values()) {
			dragPolicy.drag(e, delta);
		}
	}

	protected FXCircleSegmentHandlePart findBendTargetPart(
			FXGeometricCurvePart curvePart, EventTarget eventTarget) {
		// find last segment handle part
		Multiset<IVisualPart<Node, ? extends Node>> anchoreds = curvePart
				.getAnchoreds();
		for (IVisualPart<Node, ? extends Node> anchored : anchoreds) {
			if (anchored instanceof FXCircleSegmentHandlePart) {
				FXCircleSegmentHandlePart circleSegmentHandlePart = (FXCircleSegmentHandlePart) anchored;
				if (circleSegmentHandlePart.getSegmentParameter() == 1.0) {
					return circleSegmentHandlePart;
				}
			}
		}

		throw new IllegalStateException("Cannot find bend target part.");
	}

	protected Point getLocation(MouseEvent e) {
		Point2D location = ((FXViewer) getHost().getRoot().getViewer())
				.getCanvas().getContentGroup()
				.sceneToLocal(e.getSceneX(), e.getSceneY());
		return new Point(location.getX(), location.getY());
	}

	protected FXGeometricShapePart getShapePart() {
		return (FXGeometricShapePart) getHost().getAnchorages().keySet()
				.iterator().next();
	}

	@SuppressWarnings("serial")
	@Override
	public void press(MouseEvent e) {
		// create new curve
		FXGeometricCurve curve = new FXGeometricCurve(new Point[] {},
				FXGeometricModel.GEF_COLOR_GREEN,
				FXGeometricModel.GEF_STROKE_WIDTH,
				FXGeometricModel.GEF_DASH_PATTERN, null);
		curve.addSourceAnchorage(getShapePart().getContent());

		// create using CreationPolicy from root part
		CreationPolicy<Node> creationPolicy = getHost().getRoot()
				.getAdapter(new TypeToken<CreationPolicy<Node>>() {
				});
		creationPolicy.init();

		// find model part
		IVisualPart<Node, ? extends Node> modelPart = getHost().getRoot()
				.getChildren().get(0);
		if (!(modelPart instanceof FXGeometricModelPart)) {
			throw new IllegalStateException(
					"Cannot find FXGeometricModelPart.");
		}

		// build create operation
		creationPolicy.create(curve, (FXGeometricModelPart) modelPart,
				HashMultimap
						.<IContentPart<Node, ? extends Node>, String> create());
		ITransactionalOperation createOperation = creationPolicy.commit();

		// execute on stack
		getHost().getRoot().getViewer().getDomain().execute(createOperation);

		FXGeometricCurvePart curvePart = (FXGeometricCurvePart) getHost()
				.getRoot().getViewer().getContentPartMap().get(curve);

		// move curve to pointer location
		curvePart.getVisual().setEndPoint(getLocation(e));

		// build operation to deselect all but the new curve part
		List<IContentPart<Node, ? extends Node>> toBeDeselected = new ArrayList<>(
				getHost().getRoot().getViewer()
						.getAdapter(new TypeToken<SelectionModel<Node>>() {
						}).getSelection());
		toBeDeselected.remove(curvePart);
		DeselectOperation<Node> deselectOperation = new DeselectOperation<>(
				getHost().getRoot().getViewer(), toBeDeselected);

		// execute on stack
		getHost().getRoot().getViewer().getDomain().execute(deselectOperation);

		// find bend target part
		bendTargetPart = findBendTargetPart(curvePart, e.getTarget());

		// forward event to bend target part
		Map<AdapterKey<? extends AbstractFXOnDragPolicy>, AbstractFXOnDragPolicy> dragPolicies = bendTargetPart
				.getAdapters(FXClickDragTool.DRAG_TOOL_POLICY_KEY);
		for (AbstractFXOnDragPolicy dragPolicy : dragPolicies.values()) {
			dragPolicy.press(e);
		}
	}

	@Override
	public void release(MouseEvent e, Dimension delta) {
		if (bendTargetPart == null) {
			return;
		}

		// forward event to bend target part
		Map<AdapterKey<? extends AbstractFXOnDragPolicy>, AbstractFXOnDragPolicy> dragPolicies = bendTargetPart
				.getAdapters(FXClickDragTool.DRAG_TOOL_POLICY_KEY);
		for (AbstractFXOnDragPolicy dragPolicy : dragPolicies.values()) {
			dragPolicy.release(e, delta);
		}
	}

}
