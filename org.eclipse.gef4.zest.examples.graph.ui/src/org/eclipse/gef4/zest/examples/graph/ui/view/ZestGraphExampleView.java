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
package org.eclipse.gef4.zest.examples.graph.ui.view;

import org.eclipse.gef4.zest.examples.graph.ZestGraphExample;
import org.eclipse.gef4.zest.fx.ui.parts.ZestFxUiView;

public class ZestGraphExampleView extends ZestFxUiView {

	public ZestGraphExampleView() {
		setGraph(ZestGraphExample.createDefaultGraph());
	}

}
