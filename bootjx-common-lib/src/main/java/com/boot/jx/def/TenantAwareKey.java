package com.boot.jx.def;

import java.util.Objects;

import com.boot.jx.AppContextUtil;
import com.boot.utils.StringUtils;

public final class TenantAwareKey {
	private static final String KEY_DELIMITER = ":::";
	private static final String CODE_DELIMITER = "#";
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

	public String[] codes() {
		return StringUtils.split(code, CODE_DELIMITER);
	}

	public String toString() {
		return tenant + KEY_DELIMITER + code;
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

	public static TenantAwareKey fromCode(String... codes) {
		String code = StringUtils.join(CODE_DELIMITER, codes);
		return new TenantAwareKey(AppContextUtil.getTenant(), code);
	}

}
