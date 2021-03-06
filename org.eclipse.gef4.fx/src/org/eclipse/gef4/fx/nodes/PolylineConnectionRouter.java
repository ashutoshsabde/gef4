/*******************************************************************************
 * Copyright (c) 2014 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.fx.nodes;

import org.eclipse.gef4.geometry.planar.Point;
import org.eclipse.gef4.geometry.planar.Polyline;

/**
 * The {@link PolylineConnectionRouter} constructs a
 * {@link javafx.scene.shape.Polyline} through the supplied {@link Point}s.
 *
 * @author mwienand
 *
 */
public class PolylineConnectionRouter implements IConnectionRouter {

	@Override
	public Polyline routeConnection(Point[] points) {
		return new Polyline(points);
	}

}
