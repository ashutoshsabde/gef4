/*******************************************************************************
 * Copyright (c) 2015 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.fx.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The {@link MvcFxUiBundle} is the plug-in that integrates MVC.FX with the
 * Eclipse platform UI.
 *
 * @author anyssen
 *
 */
public class MvcFxUiBundle extends AbstractUIPlugin {

	/**
	 * The plug-in ID.
	 */
	public static final String PLUGIN_ID = "org.eclipse.gef4.mvc.fx.ui"; //$NON-NLS-1$

	// The shared instance
	private static MvcFxUiBundle plugin;

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static MvcFxUiBundle getDefault() {
		return plugin;
	}

	/**
	 * The constructor
	 */
	public MvcFxUiBundle() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

}
