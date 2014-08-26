/*******************************************************************************
 * Copyright (c) 2014 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.fx.behaviors;

import javafx.scene.Node;

import org.eclipse.gef4.common.adapt.IAdaptable;
import org.eclipse.gef4.fx.nodes.FXGeometryNode;
import org.eclipse.gef4.fx.nodes.IFXConnection;
import org.eclipse.gef4.geometry.convert.fx.JavaFX2Geometry;
import org.eclipse.gef4.geometry.planar.ICurve;
import org.eclipse.gef4.geometry.planar.IGeometry;
import org.eclipse.gef4.mvc.parts.IVisualPart;

import com.google.inject.Provider;

/**
 * @author anyssen
 */
public class DefaultVisualGeometryProvider implements
IAdaptable.Bound<IVisualPart<Node>>, Provider<IGeometry> {

	private IVisualPart<Node> host;

	@Override
	public IGeometry get() {
		Node visual = host.getVisual();

		// in case a FXGeometryNode is used, we can return its IGeometry
		if (visual instanceof IFXConnection) {
			Node curveNode = ((IFXConnection) visual).getCurveNode();
			if (curveNode instanceof FXGeometryNode) {
				return ((FXGeometryNode) curveNode).getGeometry();
			}
		} else if (visual instanceof FXGeometryNode) {
			IGeometry geometry = ((FXGeometryNode) visual).getGeometry();
			if (geometry instanceof ICurve) {
				return geometry;
			}
		}

		return JavaFX2Geometry.toRectangle(visual.getLayoutBounds());
	}

	@Override
	public IVisualPart<Node> getAdaptable() {
		return host;
	}

	@Override
	public void setAdaptable(IVisualPart<Node> adaptable) {
		this.host = adaptable;
	}

}