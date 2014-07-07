/*******************************************************************************
 * Copyright (c) 2014 itemis AG and others.
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
package org.eclipse.gef4.zest.fx.example;

import javafx.scene.Node;

import org.eclipse.gef4.mvc.parts.IHandlePartFactory;
import org.eclipse.gef4.zest.fx.ZestFxModule;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

class ZestFxExampleModule extends ZestFxModule {
	
	@Override
	protected void bindFXDefaultHandlePartFactory() {
		binder().bind(new TypeLiteral<IHandlePartFactory<Node>>() {
		}).annotatedWith(Names.named("AbstractViewer"))
				.toInstance(new FXZestExampleHandlePartFactory());
	}
	
}