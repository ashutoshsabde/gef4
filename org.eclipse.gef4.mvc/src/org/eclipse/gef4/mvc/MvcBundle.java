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
package org.eclipse.gef4.mvc;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The {@link BundleActivator} for the MVC bundle.
 */
public class MvcBundle implements BundleActivator {

	/**
	 * The plug-in id of the MVC bundle.
	 */
	public static final String PLUGIN_ID = "org.eclipse.gef4.mvc"; //$NON-NLS-1$

	private static BundleContext context;

	/**
	 * If the bundle has been started, returns the {@link BundleContext}
	 * associated to it.
	 * 
	 * @return The {@link BundleContext} of the module if this bundle was
	 *         started ({@link #start(BundleContext)}) and has since not been
	 *         stopped ( {@link #stop(BundleContext)}), <code>null</code>
	 *         otherwise.
	 */
	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		MvcBundle.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		MvcBundle.context = null;
	}

}
