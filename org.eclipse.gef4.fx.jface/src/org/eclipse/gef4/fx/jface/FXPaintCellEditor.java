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
package org.eclipse.gef4.fx.jface;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * The {@link FXPaintCellEditor} is a {@link DialogCellEditor} that is capable
 * of displaying a currently selected {@link Color} and of changing that color
 * via a dialog.
 *
 * @author anyssen
 *
 */
public class FXPaintCellEditor extends DialogCellEditor {

	private Image image;

	/**
	 * Constructs a new {@link FXPaintCellEditor}.
	 *
	 * @param parent
	 *            The parent {@link Composite}.
	 */
	public FXPaintCellEditor(Composite parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * Constructs a new {@link FXPaintCellEditor}.
	 *
	 * @param parent
	 *            The parent {@link Composite}.
	 * @param style
	 *            The SWT style for this control.
	 */
	public FXPaintCellEditor(Composite parent, int style) {
		super(parent, style);
		doSetValue(Color.TRANSPARENT);
	}

	/**
	 * Creates an {@link ImageData} filled with the given {@link Paint}. The
	 * size of the {@link ImageData} is computed so that the cell (in the
	 * property sheet table) is fully filled.
	 *
	 * @param w
	 *            The property sheet control.
	 * @param paint
	 *            The {@link Paint} to use for filling the {@link ImageData}.
	 * @return The filled {@link ImageData}.
	 */
	protected ImageData createPaintImage(Control w, Paint paint) {
		int width = 64;
		int height = 16;
		if (w instanceof Table) {
			height = ((Table) w).getItemHeight() - 1;
		} else if (w instanceof Tree) {
			height = ((Tree) w).getItemHeight() - 1;
		}
		return FXPaintSelectionDialog.createPaintImage(width, height, paint);
	}

	@Override
	public void dispose() {
		if (image != null) {
			image.dispose();
			image = null;
		}
		super.dispose();
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		FXPaintSelectionDialog dialog = new FXPaintSelectionDialog(cellEditorWindow.getShell(), "Select Fill");
		Object value = getValue();
		if (value != null) {
			dialog.setPaint((Paint) value);
		}
		int result = dialog.open();
		if (result == Window.CANCEL) {
			return value;
		} else {
			return dialog.getPaint();
		}
	}

	@Override
	protected void updateContents(Object value) {
		final Paint paint = value == null ? Color.TRANSPARENT : (Paint) value;

		if (image != null) {
			image.dispose();
		}

		ImageData id = createPaintImage(getDefaultLabel().getParent().getParent(), paint);
		image = new Image(getDefaultLabel().getDisplay(), id, id.getTransparencyMask());

		getDefaultLabel().setImage(image);
	}
}
