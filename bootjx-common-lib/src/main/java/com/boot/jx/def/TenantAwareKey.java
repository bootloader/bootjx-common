package com.boot.jx.def;

import java.util.Objects;

import com.boot.jx.AppContextUtil;

public final class TenantAwareKey {
	private final String tenant;
	private final String code;

	public TenantAwareKey(String tenant, String code) {
		this.tenant = Objects.requireNonNull(tenant, "Tenant cannot be null");
		this.code = Objects.requireNonNull(code, "Code cannot be null");
	}

	public String tenant() {
		return tenant;
	}

	public String code() {
		return code;
	}

	public String toString() {
		return tenant + "::" + code;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof TenantAwareKey))
			return false;
		TenantAwareKey that = (TenantAwareKey) o;
		return tenant.equals(that.tenant) && code.equals(that.code);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tenant, code);
	}

	public static TenantAwareKey fromCode(String code) {
		return new TenantAwareKey(AppContextUtil.getTenant(), code);
	}

}
