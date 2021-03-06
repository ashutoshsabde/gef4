/*******************************************************************************
 * Copyright (c) 2009, 2014 Fabian Steeg and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabian Steeg - initial API and implementation (see bug #277380)
 *******************************************************************************/
package org.eclipse.gef4.dot.internal.parser;

/**
 * Initialization support for running Xtext languages without equinox extension
 * registry
 */
public class DotStandaloneSetup extends DotStandaloneSetupGenerated {

	public static void doSetup() {
		new DotStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}
