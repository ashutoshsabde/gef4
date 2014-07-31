package org.eclipse.gef4.mvc.fx.example.parts;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.gef4.mvc.fx.policies.AbstractFXTypePolicy;
import org.eclipse.gef4.mvc.fx.tools.FXClickDragTool;
import org.eclipse.gef4.mvc.operations.ChangeFocusOperation;
import org.eclipse.gef4.mvc.operations.ChangeHoverOperation;
import org.eclipse.gef4.mvc.operations.ChangeSelectionOperation;
import org.eclipse.gef4.mvc.operations.ReverseUndoCompositeOperation;
import org.eclipse.gef4.mvc.parts.IContentPart;
import org.eclipse.gef4.mvc.parts.IVisualPart;
import org.eclipse.gef4.mvc.viewer.IViewer;

public abstract class AbstractFXDeleteOnTypePolicy extends AbstractFXTypePolicy {

	@Override
	public void pressed(KeyEvent event) {
		if (event.getCode() != KeyCode.DELETE) {
			return;
		}

		IViewer<Node> viewer = getHost().getRoot().getViewer();

		// prevent deletion when other policies are running
		FXClickDragTool tool = viewer.getDomain().getAdapter(
				FXClickDragTool.class);
		if (tool != null && tool.isDragging()) {
			return;
		}

		// get current selection
		List<IContentPart<Node>> currentSelection = new ArrayList<IContentPart<Node>>(
				viewer.getSelectionModel().getSelected());
		int index = currentSelection.indexOf(getHost());
		
		// remove from selection
		ChangeSelectionOperation<Node> changeSelectionOperation = null;
		if (index >= 0) {
			currentSelection.remove(index);
			changeSelectionOperation = new ChangeSelectionOperation<Node>(
					viewer, currentSelection);
		}

		// advance focus
		IContentPart<Node> newFocus = null;
		if (index < currentSelection.size()) {
			// focus next selected part
			newFocus = currentSelection.get(index);
		}
		ChangeFocusOperation<Node> changeFocusOperation = new ChangeFocusOperation<Node>(
				viewer, newFocus);

		// remove from hover (if hovered)
		IVisualPart<Node> hover = viewer.getHoverModel().getHover();
		ChangeHoverOperation<Node> changeHoverOperation = null;
		if (hover == getHost()) {
			changeHoverOperation = new ChangeHoverOperation<Node>(viewer, null);
		}

		// retrieve content operation
		IUndoableOperation changeContentOperation = getChangeContentOperation();
		
		// assemble operations
		ReverseUndoCompositeOperation revOp = new ReverseUndoCompositeOperation("Delete Shape");
		if (changeHoverOperation != null)
			revOp.add(changeHoverOperation);
		if (changeFocusOperation != null)
			revOp.add(changeFocusOperation);
		if (changeSelectionOperation != null)
			revOp.add(changeSelectionOperation);
		if (changeContentOperation != null)
			revOp.add(changeContentOperation);

		// execute on the stack
		executeOperation(revOp);
	}

	protected abstract IUndoableOperation getChangeContentOperation();

	@Override
	public void released(KeyEvent event) {
	}

}