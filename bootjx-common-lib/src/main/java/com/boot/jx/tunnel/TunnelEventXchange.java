package com.boot.jx.tunnel;

import com.boot.jx.AppParam;
import com.boot.jx.def.MCQCodecDefs;

public enum TunnelEventXchange {
	/**
	 * Event will be delivered to ALL Components
	 * 
	 * @see com.amx.jax.tunnel.ITunnelService#shout(String, Object)
	 */
	SHOUT_LISTNER("SD"), PUB_LISTNER("PL"),

	/**
	 * Events deleivered by
	 * 
	 * @see com.amx.jax.tunnel.ITunnelService#send(String, Object)
	 * 
	 */
	SEND_LISTNER("SH"),

	/**
	 * Only one worker will start working on event
	 * 
	 */
	TASK_WORKER("TW"),

	/**
	 * Multiple listeners will listen but only one worker will start working
	 * 
	 */
	TASK_LISTNER("TL"),

	/**
	 * For Audit purpose only
	 */
	AUDIT("AD");

	String queuePrefix;

	TunnelEventXchange(String queue) {
		this.queuePrefix = queue;
	}

	TunnelEventXchange() {
		this.queuePrefix = null;
	}

	private String keyPrefix() {
		return AppParam.APP_ENV.getValue() + "_C" + MCQCodecDefs.CODEC_VERSION + "_" + queuePrefix + "_";
	}

	public String getTopic(String topic) {
		return keyPrefix() + "T_" + topic;
	}

	public String getQueue(String topic) {
		return keyPrefix() + "Q_" + topic;
	}

	public String getStatusMap(String topic) {
		return keyPrefix() + "M_" + topic;
	}

	public String getEventMap(String topic) {
		return keyPrefix() + "E_" + topic;
	}

}
