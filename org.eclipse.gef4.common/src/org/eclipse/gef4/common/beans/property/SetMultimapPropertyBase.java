package org.eclipse.gef4.common.beans.property;

import java.lang.ref.WeakReference;

import org.eclipse.gef4.common.collections.ObservableSetMultimap;
import org.eclipse.gef4.common.collections.SetMultimapChangeListener;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;
import com.sun.javafx.binding.MapExpressionHelper;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * @author anyssen
 *
 * @param <K>
 *            The key type of the {@link SetMultimap}.
 * @param <V>
 *            The value type of the {@link SetMultimap}.
 */
public abstract class SetMultimapPropertyBase<K, V>
		extends SetMultimapProperty<K, V> {

	private class EmptyProperty extends ReadOnlyBooleanPropertyBase {

		@Override
		protected void fireValueChangedEvent() {
			super.fireValueChangedEvent();
		}

		@Override
		public boolean get() {
			return isEmpty();
		}

		@Override
		public Object getBean() {
			return SetMultimapPropertyBase.this;
		}

		@Override
		public String getName() {
			return "empty";
		}
	}
	private static class Listener<K, V> implements InvalidationListener {

		private final WeakReference<SetMultimapPropertyBase<K, V>> wref;

		public Listener(SetMultimapPropertyBase<K, V> ref) {
			this.wref = new WeakReference<>(ref);
		}

		@Override
		public void invalidated(Observable observable) {
			SetMultimapPropertyBase<K, V> ref = wref.get();
			if (ref == null) {
				observable.removeListener(this);
			} else {
				ref.markInvalid(ref.value);
			}
		}
	}
	private class SizeProperty extends ReadOnlyIntegerPropertyBase {
		@Override
		protected void fireValueChangedEvent() {
			super.fireValueChangedEvent();
		}

		@Override
		public int get() {
			return size();
		}

		@Override
		public Object getBean() {
			return SetMultimapPropertyBase.this;
		}

		@Override
		public String getName() {
			return "size";
		}
	}

	private Multiset<InvalidationListener> invalidationListeners = HashMultiset
			.create();

	private Multiset<ChangeListener<? super ObservableSetMultimap<K, V>>> changeListeners = HashMultiset
			.create();
	private Multiset<SetMultimapChangeListener<? super K, ? super V>> setMultimapChangeListeners = HashMultiset
			.create();
	private final SetMultimapChangeListener<K, V> setMultimapChangeListener = new SetMultimapChangeListener<K, V>() {

		@Override
		public void onChanged(
				org.eclipse.gef4.common.collections.SetMultimapChangeListener.Change<? extends K, ? extends V> change) {
			invalidateProperties();
			invalidated();
			fireValueChangedEvent(change);
		}
	};
	private ObservableSetMultimap<K, V> value;

	private ObservableValue<? extends ObservableSetMultimap<K, V>> observable = null;
	private InvalidationListener listener = null;

	private boolean valid = true;

	private SizeProperty sizeProperty;

	private EmptyProperty emptyProperty;

	/**
	 * Creates a new {@link SetMultimapPropertyBase} with no initial value.
	 */
	public SetMultimapPropertyBase() {
	}

	/**
	 * Creates a new {@link SetMultimapPropertyBase} with the given
	 * {@link ObservableSetMultimap} as initial value.
	 *
	 * @param initialValue
	 *            the initial value of the wrapped value
	 */
	public SetMultimapPropertyBase(ObservableSetMultimap<K, V> initialValue) {
		this.value = initialValue;
		if (initialValue != null) {
			initialValue.addListener(setMultimapChangeListener);
		}
	}

	@Override
	public void addListener(
			ChangeListener<? super ObservableSetMultimap<K, V>> listener) {
		changeListeners.add(listener);
	}

	@Override
	public void addListener(InvalidationListener listener) {
		invalidationListeners.add(listener);
	}

	@Override
	public void addListener(
			SetMultimapChangeListener<? super K, ? super V> listener) {
		setMultimapChangeListeners.add(listener);
	}

	@Override
	protected void appendValueToString(StringBuilder result) {
		if (isBound()) {
			result.append("bound, ");
			if (valid) {
				result.append("value: ").append(get());
			} else {
				result.append("invalid");
			}
		} else {
			result.append("value: ").append(get());
		}
	}

	@Override
	public void bind(
			final ObservableValue<? extends ObservableSetMultimap<K, V>> newObservable) {
		if (newObservable == null) {
			throw new IllegalArgumentException("Cannot bind to null");
		}
		if (!newObservable.equals(observable)) {
			unbind();
			observable = newObservable;
			if (listener == null) {
				listener = new Listener<>(this);
			}
			observable.addListener(listener);
			markInvalid(value);
		}
	}

	@Override
	public ReadOnlyBooleanProperty emptyProperty() {
		if (emptyProperty == null) {
			emptyProperty = new EmptyProperty();
		}
		return emptyProperty;
	}

	/**
	 * Fires notifications to all attached
	 * {@link javafx.beans.InvalidationListener InvalidationListeners},
	 * {@link javafx.beans.value.ChangeListener ChangeListeners}, and
	 * {@link SetMultimapChangeListener SetMultimapChangeListeners}.
	 * 
	 */
	protected void fireValueChangedEvent() {
		// TODO: implement
	}

	/**
	 * Fires notifications to all attached
	 * {@link javafx.beans.InvalidationListener InvalidationListeners},
	 * {@link javafx.beans.value.ChangeListener ChangeListeners}, and
	 * {@link SetMultimapChangeListener SetMultimapChangeListeners}.
	 *
	 * @param change
	 *            the change that needs to be propagated
	 */
	protected void fireValueChangedEvent(
			SetMultimapChangeListener.Change<? extends K, ? extends V> change) {
		// TODO: implement
	}

	@Override
	public ObservableSetMultimap<K, V> get() {
		if (!valid) {
			value = observable == null ? value : observable.getValue();
			valid = true;
			if (value != null) {
				value.addListener(setMultimapChangeListener);
			}
		}
		return value;
	}

	/**
	 * The method {@code invalidated()} can be overridden to receive
	 * invalidation notifications. This is the preferred option in
	 * {@code Objects} defining the property, because it requires less memory.
	 *
	 * The default implementation is empty.
	 */
	protected void invalidated() {
	}

	private void invalidateProperties() {
		if (sizeProperty != null) {
			sizeProperty.fireValueChangedEvent();
		}
		if (emptyProperty != null) {
			emptyProperty.fireValueChangedEvent();
		}
	}

	@Override
	public boolean isBound() {
		return observable != null;
	}

	private void markInvalid(ObservableSetMultimap<K, V> oldValue) {
		if (valid) {
			if (oldValue != null) {
				oldValue.removeListener(setMultimapChangeListener);
			}
			valid = false;
			invalidateProperties();
			invalidated();
			fireValueChangedEvent();
		}
	}

	@Override
	public void removeListener(
			ChangeListener<? super ObservableSetMultimap<K, V>> listener) {
		changeListeners.remove(listener);
	}

	@Override
	public void removeListener(InvalidationListener listener) {
		invalidationListeners.remove(listener);
	}

	@Override
	public void removeListener(
			SetMultimapChangeListener<? super K, ? super V> listener) {
		setMultimapChangeListeners.remove(listener);
	}

	@Override
	public void set(ObservableSetMultimap<K, V> newValue) {
		if (isBound()) {
			throw new IllegalArgumentException(
					(getBean() != null && getName() != null
							? getBean().getClass().getSimpleName() + "."
									+ getName() + " : "
							: "") + "A bound value cannot be set.");
		}
		if (value != newValue) {
			final ObservableSetMultimap<K, V> oldValue = value;
			value = newValue;
			markInvalid(oldValue);
		}
	}

	@Override
	public ReadOnlyIntegerProperty sizeProperty() {
		if (sizeProperty == null) {
			sizeProperty = new SizeProperty();
		}
		return sizeProperty;
	}

	@Override
	public void unbind() {
		if (observable != null) {
			value = observable.getValue();
			observable.removeListener(listener);
			observable = null;
		}
	}

}
