package com.boot.jx.def;

import java.util.Arrays;
import java.util.Objects;

import com.boot.jx.AppContextUtil;
import com.boot.utils.StringUtils;

public final class TenantAwareKey {
	private static final String KEY_DELIMITER = ":::";
	private static final String CODE_DELIMITER = "#";
	private final String tenant;
	private final String code;
	private final String[] args;
	private final String version;
	private final int hash;

	public TenantAwareKey(String tenant, String code, String... args) {
		this.tenant = Objects.requireNonNull(tenant, "Tenant cannot be null");
		this.code = Objects.requireNonNull(code, "Code cannot be null");
		this.args = args != null ? Arrays.stream(args).map(s -> s == null ? "" : s).toArray(String[]::new)
				: new String[0];
		this.version = MCQCodecDefs.CODEC_VERSION;
		this.hash = computeHash();
	}

	private int computeHash() {
		int result = Objects.hash(tenant, code, version);
		result = 31 * result + Arrays.hashCode(args);
		return result;
	}

	public String tenant() {
		return tenant;
	}

	public String code() {
		return code;
	}

	public String version() {
		return version;
	}

	public String[] args() {
		return args;
	}

	public String toString() {
		return tenant + KEY_DELIMITER + "c" + version + KEY_DELIMITER + code + CODE_DELIMITER
				+ StringUtils.join(CODE_DELIMITER, args);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof TenantAwareKey))
			return false;
		TenantAwareKey that = (TenantAwareKey) o;
		return tenant.equals(that.tenant) && code.equals(that.code) && Objects.equals(version, that.version)
				&& Arrays.equals(args, that.args);
	}

	@Override
	public int hashCode() {
		return hash;
	}

	public static TenantAwareKey fromCode(String code) {
		return new TenantAwareKey(AppContextUtil.getTenant(), code);
	}

	public static TenantAwareKey fromCodeArgs(String code, String... args) {
		return new TenantAwareKey(AppContextUtil.getTenant(), code, args);
	}

}
