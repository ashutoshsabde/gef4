/*******************************************************************************
 * Copyright (c) 2011, 2014 itemis AG and others.
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
package org.eclipse.gef4.geometry.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.gef4.geometry.internal.utils.PointListUtils;
import org.eclipse.gef4.geometry.internal.utils.PrecisionUtils;
import org.eclipse.gef4.geometry.planar.Line;
import org.eclipse.gef4.geometry.planar.Point;
import org.junit.Test;

public class PointListUtilsTests {

	@Test
	public void test_toCoordinatesArray() throws Exception {
		Point[] points = new Point[5];

		for (int i = 0; i < 5; i++) {
			points[i] = new Point(i, i);
		}

		double[] coords = PointListUtils.toCoordinatesArray(points);

		for (int i = 0; i < 10; i += 2) {
			assertTrue(PrecisionUtils.equal(points[i / 2].x, coords[i]));
			assertTrue(PrecisionUtils.equal(points[i / 2].y, coords[i + 1]));
		}
	}

	@Test
	public void test_toIntegerArray() throws Exception {
		double[] doubles = new double[10];

		for (int i = 0; i < 10; i++) {
			doubles[i] = (double) i / 2f;
		}

		int[] ints = PointListUtils.toIntegerArray(doubles);

		for (int i = 0; i < 10; i++) {
			assertTrue(PrecisionUtils.equal(ints[i], (int) doubles[i]));
		}
	}

	@Test
	public void test_toSegmentsArray() {
		Point[] points = new Point[5];

		for (int i = 0; i < points.length; i++) {
			points[i] = new Point(i, i);
		}

		Line[] segments = PointListUtils.toSegmentsArray(points, false);
		assertTrue(PrecisionUtils.equal(segments.length, points.length - 1));

		for (int i = 0; i < segments.length; i++) {
			assertTrue(segments[i].getP1().equals(points[i]));
			assertTrue(segments[i].getP2().equals(points[i + 1]));
		}

		segments = PointListUtils.toSegmentsArray(points, true);
		assertTrue(PrecisionUtils.equal(segments.length, points.length));

		for (int i = 0; i < segments.length; i++) {
			assertTrue(segments[i].getP1().equals(points[i]));
			if (i == points.length - 1) {
				assertTrue(segments[i].getP2().equals(points[0]));
			} else {
				assertTrue(segments[i].getP2().equals(points[i + 1]));
			}
		}
	}
}
