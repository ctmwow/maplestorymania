package org.ascnet.leaftown.tools;

public interface MutableValueHolder<T> extends ValueHolder<T> {
	public void setValue(T value);
}
