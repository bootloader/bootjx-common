package com.boot.jx.def;

import java.util.Arrays;
import java.util.Objects;

import com.boot.jx.AppContextUtil;
import com.boot.utils.StringUtils;

public final class TenantAwareKey {
	private static final String KEY_DELIMITER = ":::";
	private static final String CODE_DELIMITER = "#";
	private static final String DEFAULT_MODULE = "M";
	private final String module;
	private final String tenant;
	private final String code;
	private final String[] args;
	private final String version;
	private final int hash;

	private TenantAwareKey(String tenant, String module, String code, String... args) {
		this.module = module;
		this.tenant = Objects.requireNonNull(tenant, "Tenant cannot be null");
		this.code = Objects.requireNonNull(code, "Code cannot be null");
		this.args = args != null ? Arrays.stream(args).map(s -> s == null ? "" : s).toArray(String[]::new)
				: new String[0];
		this.version = MCQCodecDefs.CODEC_VERSION;
		this.hash = computeHash();
	}

	private int computeHash() {
		int result = Objects.hash(tenant, module, code, version);
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
		return keyForTenant(tenant, module, code, args);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof TenantAwareKey))
			return false;
		TenantAwareKey that = (TenantAwareKey) o;
		return tenant.equals(that.tenant) && code.equals(that.code) && Objects.equals(version, that.version)
				&& Objects.equals(module, that.module) && Arrays.equals(args, that.args);
	}

	@Override
	public int hashCode() {
		return hash;
	}

	public static TenantAwareKey fromCode(String code) {
		return new TenantAwareKey(AppContextUtil.getTenant(), DEFAULT_MODULE, code);
	}

	public static TenantAwareKey fromCodeArgs(String code, String... args) {
		return new TenantAwareKey(AppContextUtil.getTenant(), DEFAULT_MODULE, code, args);
	}

	/**
	 * This will use current tenant
	 * 
	 * @param module
	 * @param code
	 * @param args
	 * @return
	 */
	public static String key(String module, String code, String... args) {
		return keyForTenant(AppContextUtil.getTenant(), module, code, args);
	}

	private static String keyForTenant(String tenant, String module, String code, String... args) {
		return tenant + KEY_DELIMITER + module + "v" + MCQCodecDefs.CODEC_VERSION + KEY_DELIMITER + code
				+ CODE_DELIMITER + StringUtils.join(CODE_DELIMITER, args);
	}
}
