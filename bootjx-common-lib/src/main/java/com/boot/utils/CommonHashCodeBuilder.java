package com.boot.utils;

/**
 * Facade over {@link org.apache.commons.lang3.builder.HashCodeBuilder}. Prefer
 * this over importing commons-lang builders directly.
 */
public final class CommonHashCodeBuilder {

	private final org.apache.commons.lang3.builder.HashCodeBuilder delegate;

	public CommonHashCodeBuilder() {
		this.delegate = new org.apache.commons.lang3.builder.HashCodeBuilder();
	}

	public CommonHashCodeBuilder(int initialOddNumber, int multiplierOddNumber) {
		this.delegate = new org.apache.commons.lang3.builder.HashCodeBuilder(initialOddNumber, multiplierOddNumber);
	}

	public CommonHashCodeBuilder append(Object object) {
		delegate.append(object);
		return this;
	}

	public CommonHashCodeBuilder append(long value) {
		delegate.append(value);
		return this;
	}

	public CommonHashCodeBuilder append(int value) {
		delegate.append(value);
		return this;
	}

	public CommonHashCodeBuilder append(short value) {
		delegate.append(value);
		return this;
	}

	public CommonHashCodeBuilder append(char value) {
		delegate.append(value);
		return this;
	}

	public CommonHashCodeBuilder append(byte value) {
		delegate.append(value);
		return this;
	}

	public CommonHashCodeBuilder append(double value) {
		delegate.append(value);
		return this;
	}

	public CommonHashCodeBuilder append(float value) {
		delegate.append(value);
		return this;
	}

	public CommonHashCodeBuilder append(boolean value) {
		delegate.append(value);
		return this;
	}

	public CommonHashCodeBuilder append(Object[] array) {
		delegate.append(array);
		return this;
	}

	public CommonHashCodeBuilder appendSuper(int superHashCode) {
		delegate.appendSuper(superHashCode);
		return this;
	}

	public int toHashCode() {
		return delegate.toHashCode();
	}

	public Integer build() {
		return delegate.build();
	}

	@Override
	public int hashCode() {
		return delegate.toHashCode();
	}
}
