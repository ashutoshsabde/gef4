/*******************************************************************************
 * Copyright (c) 2014, 2015 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.fx.gestures;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

/**
 * An FXMouseDragGesture can be used to listen to mouse press, drag, and release
 * events. In order to use it, you have to subclass it and implement the
 * {@link #press(Node, MouseEvent)},
 * {@link #drag(Node, MouseEvent, double, double)}, and
 * {@link #release(Node, MouseEvent, double, double)} methods.
 *
 * @author mwienand
 *
 */
public abstract class AbstractMouseDragGesture extends AbstractGesture {

	// private int state = 0; // 0 = before press, 1 = after press

	private Node pressed;
	private Point2D startMousePosition;

	/**
	 * This {@link EventHandler} is registered as an event filter on the
	 * {@link Scene} to handle drag and release events.
	 */
	private EventHandler<? super MouseEvent> mouseFilter = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			onMouseEvent(event);
		}
	};

	/**
	 * This method is called upon {@link MouseEvent#MOUSE_DRAGGED} events.
	 *
	 * @param target
	 *            The event target.
	 * @param event
	 *            The corresponding {@link MouseEvent}.
	 * @param dx
	 *            The horizontal displacement from the mouse press location.
	 * @param dy
	 *            The vertical displacement from the mouse press location.
	 */
	abstract protected void drag(Node target, MouseEvent event, double dx,
			double dy);

	/**
	 * Returns the currently pressed {@link Node}.
	 *
	 * @return The currently pressed {@link Node}.
	 */
	public Node getPressed() {
		return pressed;
	}

	/**
	 * Called upon {@link MouseEvent#MOUSE_MOVED} events when no
	 * press-drag-release gesture is currently running.
	 *
	 * @param event
	 *            The underlying {@link MouseEvent}.
	 */
	protected void move(MouseEvent event) {
	}

	/**
	 * This method is called for *any* {@link MouseEvent} that occurs in the
	 * {@link Scene} where this gesture is currently registered. It processes
	 * {@link MouseEvent#MOUSE_DRAGGED} and {@link MouseEvent#MOUSE_RELEASED}
	 * events if the gesture was previously initiated (pressed node is known).
	 *
	 * @param event
	 *            The {@link MouseEvent} to process.
	 */
	protected void onMouseEvent(MouseEvent event) {
		// determine pressed/dragged/released state
		EventType<? extends Event> type = event.getEventType();
		if (pressed == null && type.equals(MouseEvent.MOUSE_MOVED)) {
			move(event);
			return;
		} else if (pressed == null && type.equals(MouseEvent.MOUSE_PRESSED)) {
			EventTarget target = event.getTarget();
			if (target instanceof Node) {
				// if (state != 0) {
				// System.err.println("(press) wrong state " + state);
				// }
				// initialize the gesture
				pressed = (Node) target;
				startMousePosition = new Point2D(event.getSceneX(),
						event.getSceneY());
				// state++;
				// System.out.println("press " +
				// System.identityHashCode(pressed)
				// + " : " + System.currentTimeMillis());
				press(pressed, event);
			}
			return;
		} else if (pressed == null) {
			// not initialized yet
			return;
		}

		if (type.equals(MouseEvent.MOUSE_EXITED_TARGET)) {
			// ignore mouse exited target events here (they may result from
			// visual changes that are caused by a preceding press)
			return;
		}

		boolean dragged = type.equals(MouseEvent.MOUSE_DRAGGED);
		boolean released = false;

		if (!dragged) {
			released = type.equals(MouseEvent.MOUSE_RELEASED);

			// FIXME: account for losing events
			if (!released) {
				if (!event.isPrimaryButtonDown()
						&& !event.isSecondaryButtonDown()
						&& !event.isMiddleButtonDown()) {
					// no button down?
					released = true;
					// System.err.println("synth release for " + type);
				}
			}
		}

		if (dragged || released) {
			double x = event.getSceneX();
			double dx = x - startMousePosition.getX();
			double y = event.getSceneY();
			double dy = y - startMousePosition.getY();
			if (dragged) {
				// if (state != 1) {
				// System.err.println("(drag) wrong state " + state);
				// }
				drag(pressed, event, dx, dy);
			} else {
				// if (state != 1) {
				// System.err.println("(release) wrong state " + state);
				// }
				// state = 0;
				// System.out.println("release " +
				// System.identityHashCode(pressed)
				// + " : " + System.currentTimeMillis());
				release(pressed, event, dx, dy);
				pressed = null;
			}
		}
	}

	/**
	 * This method is called upon {@link MouseEvent#MOUSE_PRESSED} events.
	 *
	 * @param target
	 *            The event target.
	 * @param event
	 *            The corresponding {@link MouseEvent}.
	 */
	abstract protected void press(Node target, MouseEvent event);

	@Override
	protected void register() {
		getScene().addEventFilter(MouseEvent.ANY, mouseFilter);
	}

	/**
	 * This method is called upon {@link MouseEvent#MOUSE_RELEASED} events. This
	 * method is also called for other mouse events, when a mouse release event
	 * was not fired, but was detected otherwise (probably only possible when
	 * using the JavaFX/SWT integration).
	 *
	 * @param target
	 *            The event target.
	 * @param event
	 *            The corresponding {@link MouseEvent}.
	 * @param dx
	 *            The horizontal displacement from the mouse press location.
	 * @param dy
	 *            The vertical displacement from the mouse press location.
	 */
	abstract protected void release(Node target, MouseEvent event, double dx,
			double dy);

	@Override
	protected void unregister() {
		getScene().removeEventFilter(MouseEvent.ANY, mouseFilter);
	}

}
