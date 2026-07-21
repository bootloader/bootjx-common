package com.boot.utils;

/**
 * Facade over {@link org.apache.commons.lang3.builder.EqualsBuilder}. Prefer
 * this over importing commons-lang builders directly.
 */
public final class CommonEqualsBuilder {

	private final org.apache.commons.lang3.builder.EqualsBuilder delegate = new org.apache.commons.lang3.builder.EqualsBuilder();

	public CommonEqualsBuilder append(Object lhs, Object rhs) {
		delegate.append(lhs, rhs);
		return this;
	}

	public CommonEqualsBuilder append(long lhs, long rhs) {
		delegate.append(lhs, rhs);
		return this;
	}

	public CommonEqualsBuilder append(int lhs, int rhs) {
		delegate.append(lhs, rhs);
		return this;
	}

	public CommonEqualsBuilder append(short lhs, short rhs) {
		delegate.append(lhs, rhs);
		return this;
	}

	public CommonEqualsBuilder append(char lhs, char rhs) {
		delegate.append(lhs, rhs);
		return this;
	}

	public CommonEqualsBuilder append(byte lhs, byte rhs) {
		delegate.append(lhs, rhs);
		return this;
	}

	public CommonEqualsBuilder append(double lhs, double rhs) {
		delegate.append(lhs, rhs);
		return this;
	}

	public CommonEqualsBuilder append(float lhs, float rhs) {
		delegate.append(lhs, rhs);
		return this;
	}

	public CommonEqualsBuilder append(boolean lhs, boolean rhs) {
		delegate.append(lhs, rhs);
		return this;
	}

	public CommonEqualsBuilder append(Object[] lhs, Object[] rhs) {
		delegate.append(lhs, rhs);
		return this;
	}

	public CommonEqualsBuilder appendSuper(boolean superEquals) {
		delegate.appendSuper(superEquals);
		return this;
	}

	public boolean isEquals() {
		return delegate.isEquals();
	}

	public Boolean build() {
		return delegate.build();
	}
}
