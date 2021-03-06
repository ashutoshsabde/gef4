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
 * Note: Parts of this interface have been transferred from org.eclipse.gef.EditPart.
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.parts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import com.google.common.collect.SetMultimap;

/**
 * An {@link IVisualPart} that visualizes an underlying content element.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 *              Instead, {@link AbstractContentPart} should be sub-classed.
 *
 * @author anyssen
 *
 * @param <VR>
 *            The visual root node of the UI toolkit this {@link IContentPart}
 *            is used in, e.g. javafx.scene.Node in case of JavaFX.
 *
 * @param <V>
 *            The visual node used by this {@link IContentPart}.
 *
 */
// TODO: parameterize with content type
public interface IContentPart<VR, V extends VR> extends IVisualPart<VR, V> {

	/**
	 * Property name used within {@link PropertyChangeEvent}s, which are fired
	 * whenever the {@link #setContent(Object) content} changes.
	 */
	public static final String CONTENT_PROPERTY = "content";

	/**
	 * Property name used within {@link PropertyChangeEvent}s, which are fired
	 * whenever the {@link #getContentChildren() content children} change (
	 * {@link #addContentChild(Object, int)} and
	 * {@link #removeContentChild(Object)}).
	 */
	public static final String CONTENT_CHILDREN_PROPERTY = "contentChildren";

	/**
	 * Property name used within {@link PropertyChangeEvent}s, which are fired
	 * whenever the {@link #getContentAnchorages() content anchorages} change (
	 * {@link #attachToContentAnchorage(Object, String)} and
	 * {@link #detachFromContentAnchorage(Object, String)}).
	 */
	public static final String CONTENT_ANCHORAGES_PROPERTY = "contentAnchorages";

	/**
	 * Inserts the given <i>contentChild</i> as a child to this part's content,
	 * so that it will be returned by subsequent calls to
	 * {@link #getContentChildren()}. Fires property change events using
	 * {@link #CONTENT_CHILDREN_PROPERTY} as
	 * {@link PropertyChangeEvent#getPropertyName() property name}.
	 *
	 * @param contentChild
	 *            An {@link Object} which should be added as a child to this
	 *            part's content.
	 * @param index
	 *            The index at which the <i>contentChild</i> should be added.
	 */
	public void addContentChild(Object contentChild, int index);

	/**
	 * Attaches this part's content to the given <i>contentAnchorage</i> under
	 * the specified <i>role</i>, so that it will be returned by subsequent
	 * calls to {@link #getContentAnchorages()}. Fires property change events
	 * using {@link #CONTENT_ANCHORAGES_PROPERTY} as
	 * {@link PropertyChangeEvent#getPropertyName() property name}.
	 *
	 * @param contentAnchorage
	 *            An {@link Object} to which this part's content should be
	 *            attached to.
	 * @param role
	 *            The role under which the attachment is to be established.
	 */
	public void attachToContentAnchorage(Object contentAnchorage, String role);

	/**
	 * Detaches this part's content from the given <i>contentAnchorage</i> under
	 * the specified <i>role</i>, so that it will no longer be returned by
	 * subsequent calls to {@link #getContentAnchorages()}. Fires property
	 * change events using {@link #CONTENT_ANCHORAGES_PROPERTY} as
	 * {@link PropertyChangeEvent#getPropertyName() property name}.
	 *
	 * @param contentAnchorage
	 *            An {@link Object} from which this part's content should be
	 *            detached from.
	 * @param role
	 *            The role under which the attachment is established.
	 */
	public void detachFromContentAnchorage(Object contentAnchorage,
			String role);

	/**
	 * Returns this part's content.
	 *
	 * @return This part's content.
	 */
	public Object getContent();

	/**
	 * Returns the content objects that are to be regarded as anchorages of this
	 * {@link IContentPart}'s content ({@link #getContent()}) with an (optional)
	 * role qualifier for each anchorage-anchored link that has to be
	 * established.
	 * <p>
	 * In case of a connection, one anchorage could have the "START" role, and
	 * another the "END" role. Using the role mechanism, the same anchorage may
	 * also have both roles, which can, for instance, be used for self
	 * connections.
	 *
	 * @return A {@link SetMultimap} of the content anchorages with a role to
	 *         qualify each anchorage-anchored link. If there is only a single
	 *         anchorage-anchored link to a respective anchorage, its role may
	 *         be left undefined (i.e. the map will contain an entry of the form
	 *         (anchorage, <code>null</code>)).
	 */
	public SetMultimap<? extends Object, String> getContentAnchorages();

	/**
	 * Returns a {@link List} of all of this part's content children.
	 *
	 * @return A {@link List} of all of this part's content children.
	 */
	public List<? extends Object> getContentChildren();

	/**
	 * Removes the given <i>contentChild</i> from this part's content children,
	 * so that it will no longer be returned by subsequent calls to
	 * {@link #getContentChildren()}. Fires property change events using
	 * {@link #CONTENT_CHILDREN_PROPERTY} as
	 * {@link PropertyChangeEvent#getPropertyName() property name}.
	 *
	 * @param contentChild
	 *            An {@link Object} which should be removed from this part's
	 *            content children.
	 */
	public void removeContentChild(Object contentChild);

	/**
	 * Rearranges the given <i>contentChild</i> to the new index position. Fires
	 * property change events using {@link #CONTENT_CHILDREN_PROPERTY} as
	 * {@link PropertyChangeEvent#getPropertyName() property name}.
	 *
	 * @param contentChild
	 *            The {@link Object} which is to be reordered.
	 * @param newIndex
	 *            The index to which the content child is to be reordered.
	 */
	public void reorderContentChild(Object contentChild, int newIndex);

	/**
	 * Sets this part's content to the given {@link Object value}. Fires
	 * property change events using {@link #CONTENT_PROPERTY} as property name.
	 *
	 * @param content
	 *            The new content for this part.
	 */
	public void setContent(Object content);

}
