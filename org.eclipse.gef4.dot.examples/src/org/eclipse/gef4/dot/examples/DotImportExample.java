/*******************************************************************************
 * Copyright (c) 2010, 2015 Fabian Steeg and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabian Steeg - initial API and implementation (see bug #277380)
 *******************************************************************************/
package org.eclipse.gef4.dot.examples;

import org.eclipse.gef4.dot.DotImport;
import org.eclipse.gef4.graph.Graph;

/**
 * @author Fabian Steeg (fsteeg)
 * @author anyssen
 */
public final class DotImportExample {

	public static void main(final String[] args) {
		/* We can create Graphs based on GraphViz Dot files/string */
		Graph graph = new DotImport("graph { 1--2 ; 1--3 }").newGraphInstance();
		Graph digraph = new DotImport("digraph { 1->2 ; 1->3 }")
				.newGraphInstance();

		/*
		 * We can also import GraphViz Dot files/string into an existing graph
		 */
		Graph.Builder graph2 = new Graph.Builder();
		new DotImport("digraph{1->2}").into(graph2);
		new DotImport("node[label=zested]; 2->3; 2->4").into(graph2);
		new DotImport("edge[style=dashed]; 3->5; 4->6").into(graph2);
	}

}
