/*******************************************************************************
 * Copyright (c) 2012 itemis AG and others.
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

import static org.junit.Assert.assertEquals;

import org.eclipse.gef4.geometry.euclidean.Angle;
import org.eclipse.gef4.geometry.planar.Arc;
import org.junit.Test;

public class ArcTests {

	@Test
	public void test_equals() {
		Arc a1 = new Arc(0, 0, 100, 100, Angle.fromDeg(0), Angle.fromDeg(100));
		Arc a2 = new Arc(0, 0, 100, 100,
				Angle.fromDeg(360d - TestUtils.getPrecisionFraction() / 10d),
				Angle.fromDeg(100));
		assertEquals(a1, a2);
	}

}
