package com.boot.jx.http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.boot.jx.AppContextUtil;
import com.boot.jx.logger.LoggerService;
import com.boot.utils.ArgUtil;

@Service
public class ProxyService {

	@Autowired
	CommonHttpRequest httpRequest;

	public static Logger LOGGER = LoggerService.getLogger(ProxyService.class);

	@Retryable(exclude = { HttpStatusCodeException.class }, include = Exception.class,
			backoff = @Backoff(delay = 5000, multiplier = 4.0), maxAttempts = 4)
	public ResponseEntity<String> processProxyRequest(String domain, String path, String body, HttpMethod method,
			HttpServletRequest request, HttpServletResponse response) throws URISyntaxException, MalformedURLException {
		String traceId = AppContextUtil.getTraceId();
		ThreadContext.put("traceId", traceId);
		// log if required in this line
		URI uri = new URI("https", null, domain, -1, null, null, null);
		
		
		//LOGGER.info("URL " + domain + " " + path);

		// replacing context path form urI to match actual gateway URI
		uri = UriComponentsBuilder.fromUri(uri).path(path).query(request.getQueryString()).build(true).toUri();

		HttpHeaders headers = new HttpHeaders();
		Enumeration<String> headerNames = request.getHeaderNames();

		String replacerPrefix = null;
		String replacerValue = null;
		String replacerString = httpRequest.get("x-api-replacer");
		String headerprint = httpRequest.get("x-print-log");
		if (ArgUtil.is(replacerString)) {
			String[] replacer = replacerString.toLowerCase().split(":");
			replacerPrefix = replacer[0];
			replacerValue = replacer[1];
		}

		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			String headerValue = request.getHeader(headerName);
			headers.set(headerName, headerValue);
			if (ArgUtil.is(replacerPrefix) && headerName.toLowerCase().indexOf(replacerPrefix) == 0) {
				headers.set(headerName.replaceFirst(replacerPrefix, replacerValue), headerValue);
				if (ArgUtil.is(headerprint)) {
					LOGGER.info("Header - " + headerName + " : " + headerValue);
				}
			}
			if (ArgUtil.is(headerprint)) {
				httpRequest.addHeader("Y-found", headerName);
				httpRequest.addHeader("YY-" + headerName, headerValue);
			}
		}

		headers.set("TRACE", traceId);
		headers.remove(HttpHeaders.ACCEPT_ENCODING);
		headers.remove(HttpHeaders.ORIGIN);
		headers.remove(HttpHeaders.REFERER);

		HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
		ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
		RestTemplate restTemplate = new RestTemplate(factory);
		try {

			ResponseEntity<String> serverResponse = restTemplate.exchange(uri, method, httpEntity, String.class);
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.put(HttpHeaders.CONTENT_TYPE, serverResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
			// LOGGER.info(serverResponse);
			return serverResponse;
		} catch (HttpStatusCodeException e) {
			// LOGGER.error(e.getMessage());
			return ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())
					.body(e.getResponseBodyAsString());
		}

	}

	@Recover
	public ResponseEntity<String> recoverFromRestClientErrors(Exception e, String body, HttpMethod method,
			HttpServletRequest request, HttpServletResponse response, String traceId) {
		// LOGGER.error("retry method for the following url " + request.getRequestURI()
		// + " has failed" + e.getMessage());
		// LOGGER.error("ERROR", e);
		throw new RuntimeException("There was an error trying to process you request. Please try again later");
	}

}