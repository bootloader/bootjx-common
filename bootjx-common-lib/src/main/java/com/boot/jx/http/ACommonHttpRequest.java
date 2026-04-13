package com.boot.jx.http;

import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.boot.jx.logger.LoggerService;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.HttpUtils;
import com.boot.utils.JsonUtil;

public abstract class ACommonHttpRequest {

	private static Logger LOGGER = LoggerService.getLogger(ACommonHttpRequest.class);

	public abstract HttpServletRequest getRequest();

	public abstract HttpServletResponse getResponse();

	/**
	 * 
	 * @see HttpServletRequest#getRequestURI()
	 * @return
	 */
	public String getRequestURI() {
		return getRequest().getRequestURI();
	}

	public String getServerHost() {
		return HttpUtils.getHostName(getRequest());
	}

	public String getServerName() {
		return HttpUtils.getServerName(getRequest());
	}

	public String getSubDomain() {
		return HttpUtils.getSubDomain(getRequest());
	}

	public String getBaseDomain() {
		return getServerName().replaceFirst(getSubDomain() + ".", "");
	}

	public MapModel readBody() {
		// Check Content-Type and parse JSON body if present
		String contentType = getRequest().getContentType();
		if (ArgUtil.is(contentType) && contentType.contains("application/json")) {
			try {
				String body = getRequest().getReader().lines().collect(Collectors.joining(System.lineSeparator()));

				if (ArgUtil.is(body)) {
					return MapModel.from(body);
				}
			} catch (Exception e) {
				LOGGER.warn("Error parsing JSON body", e);
			}
		}
		return MapModel.createInstance();
	}
}
