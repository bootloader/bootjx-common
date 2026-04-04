package com.boot.jx.rest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.boot.model.MapModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RestObjectAbstract<RQT, RSP, T extends RestObjectAbstract<RQT, RSP, T>> implements Serializable {

	private static final long serialVersionUID = -6143025938454052171L;

	public static class RestMapModel extends RestObjectAbstract<MapModel, MapModel, RestMapModel> {
		private static final long serialVersionUID = 1L;
	}

	public static class RestObject<RQT, RSP> extends RestObjectAbstract<RQT, RSP, RestObject<RQT, RSP>> {
		private static final long serialVersionUID = 1L;
	}

	public static class RestObjectRequest<R> {
		public RestObjectRequest(String status, R body) {
			super();
			this.status = status;
			this.body = body;
		}

		public RestObjectRequest(R body) {
			super();
			this.body = body;
		}

		public RestObjectRequest() {
			super();
		}

		private String status;

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		private R body;

		public R getBody() {
			return body;
		}

		public void setBody(R body) {
			this.body = body;
		}

	}

	private RestObjectRequest<RQT> request;
	private RestObjectRequest<RSP> response;

	private Map<String, Object> meta;

	public Map<String, Object> getMeta() {
		return meta;
	}

	public void setMeta(Map<String, Object> meta) {
		this.meta = meta;
	}

	public Map<String, Object> meta() {
		if (this.meta == null) {
			this.meta = new HashMap<String, Object>();
		}
		return this.meta;
	}

	public RestObjectAbstract() {
		super();
	}

	public RestObjectRequest<RQT> getRequest() {
		return request;
	}

	public void setRequest(RestObjectRequest<RQT> request) {
		this.request = request;
	}

	public RestObjectRequest<RSP> getResponse() {
		return response;
	}

	public void setResponse(RestObjectRequest<RSP> response) {
		this.response = response;
	}

	@SuppressWarnings("unchecked")
	public T request(RQT request) {
		this.request = new RestObjectRequest<RQT>(request);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T response(RSP response) {
		this.response = new RestObjectRequest<RSP>(response);
		return (T) this;
	}
}
