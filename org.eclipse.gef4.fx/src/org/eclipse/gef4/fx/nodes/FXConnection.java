/*******************************************************************************
 * Copyright (c) 2014 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.fx.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;

import org.eclipse.gef4.common.adapt.AdapterKey;
import org.eclipse.gef4.common.adapt.AdapterStore;
import org.eclipse.gef4.common.adapt.IAdaptable;
import org.eclipse.gef4.fx.anchors.AnchorKey;
import org.eclipse.gef4.fx.anchors.FXChopBoxAnchor;
import org.eclipse.gef4.fx.anchors.FXChopBoxAnchor.ReferencePointProvider;
import org.eclipse.gef4.fx.anchors.FXStaticAnchor;
import org.eclipse.gef4.fx.anchors.IFXAnchor;
import org.eclipse.gef4.geometry.convert.fx.Geometry2JavaFX;
import org.eclipse.gef4.geometry.convert.fx.JavaFX2Geometry;
import org.eclipse.gef4.geometry.euclidean.Angle;
import org.eclipse.gef4.geometry.euclidean.Vector;
import org.eclipse.gef4.geometry.planar.BezierCurve;
import org.eclipse.gef4.geometry.planar.ICurve;
import org.eclipse.gef4.geometry.planar.Line;
import org.eclipse.gef4.geometry.planar.Point;

public class FXConnection extends Group {

	/**
	 * The {@link FXChopBoxHelper} can be registered for an {@link FXConnection}
	 * and serves as a {@link ReferencePointProvider} for all {@link AnchorKey}s
	 * of that {@link FXConnection} which are registered at
	 * {@link FXChopBoxAnchor}s.
	 *
	 * @author wienand
	 *
	 */
	public static class FXChopBoxHelper implements
			FXChopBoxAnchor.ReferencePointProvider {

		/**
		 * The {@link ReferencePointMap} is used to store the reference points
		 * for the individual {@link AnchorKey}s. A reference point is computed
		 * whenever it is requested (i.e. {@link #get(Object)} is called).
		 * Currently, the computation is only performed if no reference point is
		 * available (i.e. on the anchor attachment). In order to query a
		 * currently set reference point, you can use {@link #getRaw(Object)},
		 * which will never trigger a reference point computation, but instead
		 * simply look it up in the map.
		 *
		 * @author wienand
		 *
		 */
		public class ReferencePointMap extends HashMap<AnchorKey, Point> {

			private static final long serialVersionUID = 1L;

			@Override
			public Point get(Object key) {
				if (!(key instanceof AnchorKey)) {
					throw new IllegalArgumentException(
							"Expected AnchorKey but got <" + key + ">");
				}

				AnchorKey ak = (AnchorKey) key;
				if (!containsKey(ak)) {
					if (!(connection.getStartAnchor() == null || connection
							.getEndAnchor() == null)) {
						updateReferencePoint(connection.getAnchorIndex(ak), ak);
					} else {
						put(ak, new Point());
					}
				}
				return super.get(ak);
			}

			/**
			 * Does not compute a value for the given <i>key</i> but returns the
			 * currently stored value instead.
			 *
			 * @param key
			 *            The key for which to look up the value.
			 * @return The value currently stored at the given <i>key</i>.
			 */
			public Point getRaw(Object key) {
				return super.get(key);
			}

		}

		// need to hold a reference to the ReferencePointMap in order to be able
		// to call #getRaw().
		private ReferencePointMap referencePoints = new ReferencePointMap();
		private ReadOnlyMapWrapper<AnchorKey, Point> referencePointProperty = new ReadOnlyMapWrapper<AnchorKey, Point>(
				FXCollections.observableMap(referencePoints));

		/**
		 * Manages the addition and removal of position-change-listeners for the
		 * {@link AnchorKey}s of the {@link FXConnection}.
		 */
		private MapChangeListener<AnchorKey, IFXAnchor> anchorsChangeListener = new MapChangeListener<AnchorKey, IFXAnchor>() {
			@Override
			public void onChanged(
					javafx.collections.MapChangeListener.Change<? extends AnchorKey, ? extends IFXAnchor> change) {
				AnchorKey key = change.getKey();
				IFXAnchor oldAnchor = change.getValueRemoved();
				if (oldAnchor != null && pcls.containsKey(key)) {
					oldAnchor.positionProperty().removeListener(
							pcls.remove(key));
					updateReferencePoints(null);
				}
				IFXAnchor newAnchor = change.getValueAdded();
				if (newAnchor != null) {
					MapChangeListener<? super AnchorKey, ? super Point> pcl = createPCL(
							newAnchor, key);
					pcls.put(key, pcl);
					newAnchor.positionProperty().addListener(pcl);
				}
			}
		};

		/**
		 * {@link FXConnection} to work with.
		 */
		private FXConnection connection;

		/**
		 * Map to store/manage position change listeners for individual
		 * {@link AnchorKey}s.
		 */
		private Map<AnchorKey, MapChangeListener<? super AnchorKey, ? super Point>> pcls = new HashMap<AnchorKey, MapChangeListener<? super AnchorKey, ? super Point>>();

		public FXChopBoxHelper(FXConnection connection) {
			this.connection = connection;
			/*
			 * If the map behind the anchors-property is replaced, we have to
			 * update our anchorsChangeListener accordingly.
			 */
			connection.anchorsProperty().addListener(
					new ChangeListener<ObservableMap<AnchorKey, IFXAnchor>>() {
						@Override
						public void changed(
								ObservableValue<? extends ObservableMap<AnchorKey, IFXAnchor>> observable,
								ObservableMap<AnchorKey, IFXAnchor> oldValue,
								ObservableMap<AnchorKey, IFXAnchor> newValue) {
							if (oldValue == newValue) {
								return;
							}
							if (oldValue != null) {
								oldValue.removeListener(anchorsChangeListener);
							}
							if (newValue != null) {
								newValue.addListener(anchorsChangeListener);
							}
						}
					});
			connection.anchorsProperty().addListener(anchorsChangeListener);
		}

		private MapChangeListener<? super AnchorKey, ? super Point> createPCL(
				final IFXAnchor anchor, final AnchorKey key) {
			return new MapChangeListener<AnchorKey, Point>() {
				@Override
				public void onChanged(
						javafx.collections.MapChangeListener.Change<? extends AnchorKey, ? extends Point> change) {
					if (change.wasAdded()) {
						if (change.getKey().equals(key)
								&& anchor.isAttached(key)) {
							updateReferencePoints(key);
						}
					}
				}
			};
		}

		// TODO: move to utility && replace with safe algorithm
		private Point getCenter(Node anchorageNode) {
			Point center = JavaFX2Geometry.toRectangle(
					connection.sceneToLocal(anchorageNode
							.localToScene(anchorageNode.getLayoutBounds())))
					.getCenter();
			if (Double.isNaN(center.x) || Double.isNaN(center.y)) {
				return null;
			}
			return center;
		}

		private Point getNeighbor(int anchorIndex, int step) {
			List<IFXAnchor> anchors = connection.getAnchors();
			IFXAnchor anchor = anchors.get(anchorIndex);
			if (!(anchor instanceof FXChopBoxAnchor)) {
				throw new IllegalStateException(
						"specified anchor is no FXChopBoxAnchor");
			}
			Node anchorage = anchor.getAnchorage();

			// first uncontained static anchor (no anchorage)
			// or first anchorage center
			for (int i = anchorIndex + step; i < anchors.size() && i >= 0; i += step) {
				IFXAnchor predAnchor = anchors.get(i);
				if (predAnchor == null) {
					throw new IllegalStateException(
							"connection inconsistent (null anchor)");
				}
				Node predAnchorage = predAnchor.getAnchorage();
				if (predAnchorage == null) {
					// anchor is static
					AnchorKey anchorKey = connection.getAnchorKey(i);
					Point position = predAnchor.getPosition(anchorKey);
					if (position == null) {
						throw new IllegalStateException(
								"connection inconsistent (null position)");
					}
					Point2D local = anchorage.sceneToLocal(connection
							.localToScene(position.x, position.y));
					if (!anchorage.contains(local)) {
						return position;
					}
				} else {
					// anchor position depends on anchorage
					Point position = getCenter(predAnchorage);
					if (position == null) {
						throw new IllegalStateException(
								"cannot determine anchorage center");
					}
					return position;
				}
			}

			// no neighbor found
			return null;
		}

		private Point getPred(int anchorIndex) {
			return getNeighbor(anchorIndex, -1);
		}

		private Point getSucc(int anchorIndex) {
			return getNeighbor(anchorIndex, 1);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.gef4.fx.nodes.FXChopBoxReferencePointProvider#
		 * referencePointProperty()
		 */
		@Override
		public ReadOnlyMapWrapper<AnchorKey, Point> referencePointProperty() {
			return referencePointProperty;
		}

		private void updateReferencePoint(int anchorIndex, AnchorKey key) {
			// FIXME: cannot query connection if start/end is unset
			if (connection.getStartAnchor() == null
					|| connection.getEndAnchor() == null) {
				return;
			}

			// only compute reference points for chop box anchors
			if (!(connection.getAnchors().get(anchorIndex) instanceof FXChopBoxAnchor)) {
				return;
			}

			// get old reference point
			Point oldRef = referencePoints.getRaw(key);

			// compute new reference point
			Point newRef = null;
			Point pred = getPred(anchorIndex);
			Point succ = getSucc(anchorIndex);
			if (pred == null && succ == null) {
				/*
				 * Neither predecessor nor successor can be identified. This can
				 * happen for the initialization of connections when a static
				 * position is inside the anchorage of the current anchor. This
				 * means, the reference point that is returned now will be
				 * discarded in a succeeding call (we have to come up with some
				 * value here for the FXChopBoxAnchor to work with).
				 */
				newRef = new Point();
			} else if (pred != null) {
				newRef = pred;
			} else if (succ != null) {
				newRef = succ;
			} else {
				newRef = new Line(pred, succ).get(0.5);
			}

			// only update if necessary (when it changes)
			if (oldRef == null || !newRef.equals(oldRef)) {
				referencePointProperty.put(key, newRef);
			}
		}

		private void updateReferencePoints(AnchorKey key) {
			// FIXME: cannot query connection if start/end is unset
			if (connection.getStartAnchor() == null
					|| connection.getEndAnchor() == null) {
				return;
			}

			int anchorIndex = key == null ? -1 : connection.getAnchorIndex(key);
			List<IFXAnchor> anchors = connection.getAnchors();
			for (int i = 0; i < anchors.size(); i++) {
				// we do not have to update the reference point for the
				// given key, because the corresponding position just
				// changed, so it was updated already
				if (anchorIndex == i) {
					continue;
				}
				updateReferencePoint(i, connection.getAnchorKey(i));
			}
		}

	}

	/**
	 * CSS class assigned to decoration visuals.
	 */
	public static final String CSS_CLASS_DECORATION = "decoration";

	/**
	 * The <i>id</i> used to identify the start point of this connection at the
	 * start anchor.
	 */
	private static final String START_ROLE = "start";

	/**
	 * The <i>id</i> used to identify the end point of this connection at the
	 * end anchor.
	 */
	private static final String END_ROLE = "end";

	/**
	 * Prefix for the default <i>ids</i> used by this connection to identify
	 * specific way points at way point anchors.
	 */
	private static final String WAY_POINT_ROLE_PREFIX = "waypoint-";

	// visuals
	private FXGeometryNode<ICurve> curveNode = new FXGeometryNode<ICurve>();
	private IFXConnectionRouter router = new FXPolylineConnectionRouter();

	// used to pass as argument to IFXAnchor#attach() and #detach()
	private AdapterStore as = new AdapterStore();

	// TODO: use ReadOnlyObjectWrapper (JavaFX Property) for decorations
	private IFXDecoration startDecoration = null;
	private IFXDecoration endDecoration = null;
	private ReadOnlyMapWrapper<AnchorKey, IFXAnchor> anchorsProperty = new ReadOnlyMapWrapper<AnchorKey, IFXAnchor>(
			FXCollections.<AnchorKey, IFXAnchor> observableHashMap());

	private List<AnchorKey> wayAnchorKeys = new ArrayList<AnchorKey>();
	private int nextWayAnchorId = 0;

	// refresh geometry on position changes
	private boolean inRefresh = false;
	private Map<AnchorKey, MapChangeListener<? super AnchorKey, ? super Point>> anchorKeyPCL = new HashMap<AnchorKey, MapChangeListener<? super AnchorKey, ? super Point>>();

	public FXConnection() {
		// disable resizing children which would change their layout positions
		// in some cases
		setAutoSizeChildren(false);

		// register any adapters that will be needed during attach() and
		// detach() at anchors
		registerAnchorInfos(as);
	}

	public void addWayAnchor(int index, IFXAnchor anchor) {
		if (anchor == null) {
			throw new IllegalArgumentException("anchor may not be null.");
		}

		AnchorKey anchorKey = generateWayAnchorKey();
		// assert(!anchorKeyPCL.containsKey(anchorKey));
		putAnchor(anchor, anchorKey, index);
	}

	public void addWayPoint(int index, Point wayPointInLocal) {
		FXStaticAnchor anchor = new FXStaticAnchor(
				JavaFX2Geometry.toPoint(localToScene(wayPointInLocal.x,
						wayPointInLocal.y)));
		addWayAnchor(index, anchor);
	}

	protected ReadOnlyMapProperty<AnchorKey, IFXAnchor> anchorsProperty() {
		return anchorsProperty.getReadOnlyProperty();
	}

	/**
	 * Arranges the given decoration according to the passed-in values. Returns
	 * the transformed end point of the arranged decoration.
	 *
	 * @param deco
	 * @param start
	 * @param direction
	 * @param decoStart
	 * @param decoDirection
	 * @return the transformed end point of the arranged decoration
	 */
	protected Point arrangeDecoration(IFXDecoration deco, Point start,
			Vector direction, Point decoStart, Vector decoDirection) {
		Node visual = deco.getVisual();

		// position
		visual.setLayoutX(start.x);
		visual.setLayoutY(start.y);

		// rotation
		Angle angleCW = null;
		if (!direction.isNull() && !decoDirection.isNull()) {
			angleCW = decoDirection.getAngleCW(direction);
			visual.getTransforms().clear();
			visual.getTransforms().add(new Rotate(angleCW.deg(), 0, 0));
		}

		// return corresponding curve point
		return angleCW == null ? start : start.getTranslated(decoDirection
				.getRotatedCW(angleCW).toPoint());
	}

	protected void arrangeEndDecoration() {
		if (endDecoration == null) {
			return;
		}

		// determine curve end point and curve end direction
		Point endPoint = getEndPoint();
		ICurve curve = getCurveNode().getGeometry();
		if (curve == null) {
			return;
		}

		BezierCurve[] beziers = curve.toBezier();
		if (beziers.length == 0) {
			return;
		}

		BezierCurve endDerivative = beziers[beziers.length - 1].getDerivative();
		Point slope = endDerivative.get(1);
		if (slope.equals(0, 0)) {
			/*
			 * This is the case when beziers[-1] is a degenerated curve where
			 * the last control point equals the end point. As a work around, we
			 * evaluate the derivative at t = 0.99.
			 */
			slope = endDerivative.get(0.99);
		}
		Vector endDirection = new Vector(slope.getNegated());

		// determine decoration start point and decoration direction
		Point decoStartPoint = endDecoration.getLocalStartPoint();
		Point decoEndPoint = endDecoration.getLocalEndPoint();
		Vector decoDirection = new Vector(decoStartPoint, decoEndPoint);

		arrangeDecoration(endDecoration, endPoint, endDirection,
				decoStartPoint, decoDirection);
	}

	protected void arrangeStartDecoration() {
		if (startDecoration == null) {
			return;
		}

		// determine curve start point and curve start direction
		Point startPoint = getStartPoint();
		ICurve curve = getCurveNode().getGeometry();
		if (curve == null) {
			return;
		}

		BezierCurve[] beziers = curve.toBezier();
		if (beziers.length == 0) {
			return;
		}

		BezierCurve startDerivative = beziers[0].getDerivative();
		Point slope = startDerivative.get(0);
		if (slope.equals(0, 0)) {
			/*
			 * This is the case when beziers[0] is a degenerated curve where the
			 * start point equals the first control point. As a work around, we
			 * evaluate the derivative at t = 0.01.
			 */
			slope = startDerivative.get(0.01);
		}
		Vector curveStartDirection = new Vector(slope);

		// determine decoration start point and decoration start direction
		Point decoStartPoint = startDecoration.getLocalStartPoint();
		Point decoEndPoint = startDecoration.getLocalEndPoint();
		Vector decoDirection = new Vector(decoStartPoint, decoEndPoint);

		arrangeDecoration(startDecoration, startPoint, curveStartDirection,
				decoStartPoint, decoDirection);
	}

	protected MapChangeListener<? super AnchorKey, ? super Point> createPCL(
			final AnchorKey anchorKey) {
		return new MapChangeListener<AnchorKey, Point>() {
			@Override
			public void onChanged(
					javafx.collections.MapChangeListener.Change<? extends AnchorKey, ? extends Point> change) {
				if (change.getKey().equals(anchorKey)) {
					refreshGeometry();
				}
			}
		};
	}

	protected AnchorKey generateWayAnchorKey() {
		if (nextWayAnchorId == Integer.MAX_VALUE) {
			List<IFXAnchor> wayAnchors = getWayAnchors();
			removeAllWayPoints();
			nextWayAnchorId = 0;
			setWayAnchors(wayAnchors);
		}
		return new AnchorKey(getCurveNode(), WAY_POINT_ROLE_PREFIX
				+ nextWayAnchorId++);
	}

	protected int getAnchorIndex(AnchorKey anchorKey) {
		if (anchorKey.equals(getStartAnchorKey())) {
			return 0;
		} else if (anchorKey.equals(getEndAnchorKey())) {
			return getAnchors().size() - 1;
		} else {
			return getWayIndex(anchorKey) + 1;
		}
	}

	protected AnchorKey getAnchorKey(int anchorIndex) {
		if (anchorIndex < 0 || anchorIndex >= getAnchors().size()) {
			throw new IllegalArgumentException(
					"The given anchor index is out of bounds.");
		}

		if (anchorIndex == 0) {
			return getStartAnchorKey();
		} else if (anchorIndex == getAnchors().size() - 1) {
			return getEndAnchorKey();
		} else {
			return getWayAnchorKey(anchorIndex - 1);
		}
	}

	public List<IFXAnchor> getAnchors() {
		int wayPointCount = getWayAnchorsSize();
		List<IFXAnchor> anchors = new ArrayList<>(wayPointCount + 2);

		// start anchor
		IFXAnchor startAnchor = getStartAnchor();
		if (startAnchor == null) {
			throw new IllegalStateException("Start anchor may never be null.");
		}
		anchors.add(startAnchor);

		// way anchors
		anchors.addAll(getWayAnchors());

		// end anchor
		IFXAnchor endAnchor = getEndAnchor();
		if (endAnchor == null) {
			throw new IllegalStateException("End anchor may never be null.");
		}
		anchors.add(endAnchor);

		return anchors;
	}

	public FXGeometryNode<ICurve> getCurveNode() {
		return curveNode;
	}

	public IFXAnchor getEndAnchor() {
		return anchorsProperty.get(getEndAnchorKey());
	}

	protected AnchorKey getEndAnchorKey() {
		return new AnchorKey(getCurveNode(), END_ROLE);
	}

	public IFXDecoration getEndDecoration() {
		return endDecoration;
	}

	public Point getEndPoint() {
		IFXAnchor anchor = getEndAnchor();
		if (anchor == null) {
			return null;
		}
		if (!anchor.isAttached(getEndAnchorKey())) {
			return null;
		}
		return JavaFX2Geometry.toPoint(getCurveNode()
				.localToParent(
						Geometry2JavaFX.toFXPoint(anchor
								.getPosition(getEndAnchorKey()))));
	}

	public Point[] getPoints() {
		int wayPointCount = getWayAnchorsSize();
		Point[] points = new Point[wayPointCount + 2];

		points[0] = getStartPoint();
		if (points[0] == null) {
			return new Point[] {};
		}

		for (int i = 0; i < wayPointCount; i++) {
			points[i + 1] = getWayPoint(i);
			if (points[i + 1] == null) {
				return new Point[] {};
			}
		}

		points[points.length - 1] = getEndPoint();
		if (points[points.length - 1] == null) {
			return new Point[] {};
		}

		return points;
	}

	public IFXConnectionRouter getRouter() {
		return router;
	}

	public IFXAnchor getStartAnchor() {
		return anchorsProperty.get(getStartAnchorKey());
	}

	protected AnchorKey getStartAnchorKey() {
		return new AnchorKey(getCurveNode(), START_ROLE);
	}

	public IFXDecoration getStartDecoration() {
		return startDecoration;
	}

	public Point getStartPoint() {
		IFXAnchor anchor = getStartAnchor();
		if (anchor == null) {
			return null;
		}
		if (!anchor.isAttached(getStartAnchorKey())) {
			return null;
		}
		return JavaFX2Geometry.toPoint(getCurveNode().localToParent(
				Geometry2JavaFX.toFXPoint(anchor
						.getPosition(getStartAnchorKey()))));
	}

	public IFXAnchor getWayAnchor(int index) {
		return anchorsProperty.get(getWayAnchorKey(index));
	}

	protected AnchorKey getWayAnchorKey(int index) {
		if (0 <= index && index < wayAnchorKeys.size()) {
			return wayAnchorKeys.get(index);
		}
		return null;
	}

	public List<IFXAnchor> getWayAnchors() {
		int wayPointsCount = getWayAnchorsSize();
		List<IFXAnchor> wayPointAnchors = new ArrayList<IFXAnchor>(
				wayPointsCount);
		for (int i = 0; i < wayPointsCount; i++) {
			IFXAnchor wayAnchor = getWayAnchor(i);
			if (wayAnchor == null) {
				throw new IllegalStateException("Way anchor may never be null.");
			}
			wayPointAnchors.add(wayAnchor);
		}
		return wayPointAnchors;
	}

	public int getWayAnchorsSize() {
		return wayAnchorKeys.size();
	}

	protected int getWayIndex(AnchorKey key) {
		int index = wayAnchorKeys.indexOf(key);
		if (index == -1) {
			throw new IllegalArgumentException(
					"The given AnchorKey ("
							+ key
							+ ") is not registered as a way point anchor for this connection.");
		}
		return index;
	}

	public Point getWayPoint(int index) {
		IFXAnchor anchor = getWayAnchor(index);
		if (anchor == null) {
			throw new IllegalArgumentException("No waypoint at index " + index);
		}
		if (!anchor.isAttached(getWayAnchorKey(index))) {
			return null;
		}
		return JavaFX2Geometry.toPoint(getCurveNode().localToParent(
				Geometry2JavaFX.toFXPoint(anchor
						.getPosition(getWayAnchorKey(index)))));
	}

	public List<Point> getWayPoints() {
		List<IFXAnchor> wayPointAnchors = getWayAnchors();
		List<Point> wayPoints = new ArrayList<Point>(wayPointAnchors.size());
		for (int i = 0; i < wayPointAnchors.size(); i++) {
			wayPoints.add(wayPointAnchors.get(i)
					.getPosition(getWayAnchorKey(i)));
		}
		return wayPoints;
	}

	public boolean isEndConnected() {
		IFXAnchor anchor = getEndAnchor();
		return anchor != null && anchor.getAnchorage() != null
				&& anchor.getAnchorage() != this;
	}

	public boolean isStartConnected() {
		IFXAnchor anchor = getStartAnchor();
		return anchor != null && anchor.getAnchorage() != null
				&& anchor.getAnchorage() != this;
	}

	public boolean isWayConnected(int index) {
		IFXAnchor anchor = getWayAnchor(index);
		return anchor.getAnchorage() != null && anchor.getAnchorage() != this;
	}

	protected void putAnchor(IFXAnchor anchor, AnchorKey anchorKey, int wayIndex) {
		/*
		 * IMPORTANT: The anchor is put into the map before attaching it, so
		 * that listeners on the map can register position change listeners on
		 * the anchor (but cannot query its position, yet).
		 */
		if (!anchorKey.equals(getStartAnchorKey())
				&& !anchorKey.equals(getEndAnchorKey())) {
			wayAnchorKeys.add(wayIndex, anchorKey);
		}
		anchorsProperty.put(anchorKey, anchor);
		anchor.attach(anchorKey, as);
		if (!anchorKeyPCL.containsKey(anchorKey)) {
			MapChangeListener<? super AnchorKey, ? super Point> pcl = createPCL(anchorKey);
			anchorKeyPCL.put(anchorKey, pcl);
			anchor.positionProperty().addListener(pcl);
		}
		refreshGeometry();
	}

	protected void refreshGeometry() {
		// TODO: this should not be here
		// guard against recomputing the curve while recomputing the curve
		if (inRefresh) {
			return;
		}

		ICurve newGeometry = router.routeConnection(getPoints());
		if (curveNode != null && curveNode.getGeometry() != null
				&& curveNode.getGeometry().equals(newGeometry)) {
			return;
		}
		inRefresh = true;

		// clear current visuals
		getChildren().clear();

		// compute new curve (this can lead to another refreshGeometry() call
		// which is not executed)
		curveNode.setGeometry(newGeometry);

		// z-order decorations above curve
		getChildren().add(curveNode);
		if (startDecoration != null) {
			getChildren().add(startDecoration.getVisual());
			arrangeStartDecoration();
		}
		if (endDecoration != null) {
			getChildren().add(endDecoration.getVisual());
			arrangeEndDecoration();
		}

		inRefresh = false;
	}

	protected void registerAnchorInfos(IAdaptable adaptable) {
		// register an FXChopBoxHelper, which is passed to the attached anchors.
		adaptable.setAdapter(AdapterKey.get(ReferencePointProvider.class),
				new FXChopBoxHelper(this));
	}

	public void removeAllWayPoints() {
		for (int i = getWayAnchorsSize() - 1; i >= 0; i--) {
			removeWayPoint(i);
		}
	}

	protected void removeAnchor(AnchorKey anchorKey, IFXAnchor oldAnchor) {
		if (anchorKeyPCL.containsKey(anchorKey)) {
			oldAnchor.positionProperty().removeListener(
					anchorKeyPCL.remove(anchorKey));
		}
		/*
		 * Important: detach() after removing from the anchors-map, so that
		 * listeners on the anchors-map can retrieve the anchor position.
		 */
		if (wayAnchorKeys.contains(anchorKey)) {
			// remove from way anchor keys so that the anchors.size is
			// consistent with the way anchor size + (start present) + (end
			// present)
			wayAnchorKeys.remove(anchorKey);
		}
		anchorsProperty.remove(anchorKey);
		oldAnchor.detach(anchorKey, as);
	}

	public void removeWayPoint(int index) {
		// check index out of range
		if (index < 0 || index >= getWayAnchorsSize()) {
			throw new IllegalArgumentException("Index out of range (index: "
					+ index + ", size: " + getWayAnchorsSize() + ").");
		}

		AnchorKey anchorKey = getWayAnchorKey(index);
		if (!anchorsProperty.containsKey(anchorKey)) {
			throw new IllegalStateException(
					"Inconsistent state: way anchor not in map!");
		}

		IFXAnchor oldAnchor = anchorsProperty.get(anchorKey);
		removeAnchor(anchorKey, oldAnchor);

		refreshGeometry();
	}

	public void setAnchors(java.util.List<IFXAnchor> anchors) {
		if (anchors.size() < 2) {
			throw new IllegalArgumentException(
					"start end end anchors have to be provided.");
		}
		setStartAnchor(anchors.get(0));
		if (anchors.size() > 2) {
			setWayAnchors(anchors.subList(1, anchors.size() - 1));
		} else {
			removeAllWayPoints();
		}
		setEndAnchor(anchors.get(anchors.size() - 1));
	}

	public void setEndAnchor(IFXAnchor anchor) {
		if (anchor == null) {
			throw new IllegalArgumentException("anchor may not be null.");
		}

		AnchorKey anchorKey = getEndAnchorKey();
		IFXAnchor oldAnchor = anchorsProperty.get(anchorKey);
		if (oldAnchor != anchor) {
			if (oldAnchor != null) {
				removeAnchor(anchorKey, oldAnchor);
			}
			putAnchor(anchor, anchorKey, -1);
		}
	}

	public void setEndDecoration(IFXDecoration endDeco) {
		endDecoration = endDeco;
		if (endDecoration != null) {
			ObservableList<String> styleClasses = endDecoration.getVisual()
					.getStyleClass();
			if (!styleClasses.contains(CSS_CLASS_DECORATION)) {
				styleClasses.add(CSS_CLASS_DECORATION);
			}
		}
		refreshGeometry();
	}

	public void setEndPoint(Point endPointInLocal) {
		FXStaticAnchor anchor = new FXStaticAnchor(
				JavaFX2Geometry.toPoint(localToScene(endPointInLocal.x,
						endPointInLocal.y)));
		setEndAnchor(anchor);
	}

	public void setRouter(IFXConnectionRouter router) {
		this.router = router;
		refreshGeometry();
	}

	public void setStartAnchor(IFXAnchor anchor) {
		if (anchor == null) {
			throw new IllegalArgumentException("anchor may not be null.");
		}

		AnchorKey anchorKey = getStartAnchorKey();
		IFXAnchor oldAnchor = anchorsProperty.get(anchorKey);
		if (oldAnchor != anchor) {
			if (oldAnchor != null) {
				removeAnchor(anchorKey, oldAnchor);
			}
			putAnchor(anchor, anchorKey, -1);
		}
	}

	public void setStartDecoration(IFXDecoration startDeco) {
		startDecoration = startDeco;
		if (startDecoration != null) {
			ObservableList<String> styleClasses = startDecoration.getVisual()
					.getStyleClass();
			if (!styleClasses.contains(CSS_CLASS_DECORATION)) {
				styleClasses.add(CSS_CLASS_DECORATION);
			}
		}
		refreshGeometry();
	}

	public void setStartPoint(Point startPointInLocal) {
		FXStaticAnchor anchor = new FXStaticAnchor(
				JavaFX2Geometry.toPoint(localToScene(startPointInLocal.x,
						startPointInLocal.y)));
		setStartAnchor(anchor);
	}

	public void setWayAnchor(int index, IFXAnchor anchor) {
		if (index < 0 || index >= wayAnchorKeys.size()) {
			throw new IllegalArgumentException("index out of range.");
		}
		if (anchor == null) {
			throw new IllegalArgumentException("anchor may not be null.");
		}

		AnchorKey anchorKey = getWayAnchorKey(index);
		IFXAnchor oldAnchor = anchorsProperty.get(anchorKey);
		if (oldAnchor != anchor) {
			if (oldAnchor != null) {
				removeAnchor(anchorKey, oldAnchor);
			}
			putAnchor(anchor, anchorKey, index);
		}
	}

	public void setWayAnchors(List<IFXAnchor> anchors) {
		int wayPointsSize = getWayAnchorsSize();
		// IMPORTANT: We have to do the removal of way anchors before
		// changing/adding anchors.
		for (int i = wayPointsSize - 1; i >= anchors.size(); i--) {
			removeWayPoint(i);
		}
		for (int i = 0; i < wayPointsSize && i < anchors.size(); i++) {
			setWayAnchor(i, anchors.get(i));
		}
		for (int i = wayPointsSize; i < anchors.size(); i++) {
			addWayAnchor(i, anchors.get(i));
		}
	}

	public void setWayPoint(int index, Point wayPointInLocal) {
		FXStaticAnchor anchor = new FXStaticAnchor(
				JavaFX2Geometry.toPoint(localToScene(wayPointInLocal.x,
						wayPointInLocal.y)));
		setWayAnchor(index, anchor);
	}

	public void setWayPoints(List<Point> wayPoints) {
		int waySize = wayAnchorKeys.size();
		int i = 0;
		for (; i < waySize && i < wayPoints.size(); i++) {
			setWayPoint(i, wayPoints.get(i));
		}
		for (; i < wayPoints.size(); i++) {
			addWayPoint(i, wayPoints.get(i));
		}
		for (; i < waySize; i++) {
			removeWayPoint(i);
		}
	}

}