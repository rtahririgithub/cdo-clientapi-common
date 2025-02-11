/*
 *  Copyright (c) 2023 TELUS Communications Inc.,
 *  All Rights Reserved.
 *
 *  This document contains proprietary information that shall be
 *  distributed or routed only within TELUS, and its authorized
 *  clients, except with written permission of TELUS.
 *
 * $Id$
 */

package com.telus.cis.common.web.security;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class AuthorizedClient {
	
	private static Map<String, AccessList> accessUrlPatternMap = new HashMap<>();
	private static Map<String, AuthorizedClientDetail> clientMap = new HashMap<>();

	public AuthorizedClient(Map<String, AuthorizedClientDetail> users) {
		users.values().forEach(client -> {
			clientMap.put(client.getClientId(), client);
			accessUrlPatternMap.put(client.getClientId(), client.getAccessList());
			log.info("{} : {}", client.getClientName(), isNull(client.getAccessList()) ? "accessList undefined" : client.getAccessList().toString());

		});
	}

	public AuthorizedClientDetail getClient(String clientId) {
		return clientMap.get(clientId);
	}

	/**
	 * Return true if the given clientId is allowed to access to the incoming request url.
	 */
	public boolean allowedClient(HttpServletRequest request, String clientId) {

		AccessList accessUrlPatternList = accessUrlPatternMap.get(clientId);
		log.debug("clientId: [{}], uri: [{}], accessUrlPatternList: [{}]", clientId, request.getRequestURI(), accessUrlPatternList);

		if (accessUrlPatternList == null) {
			log.info("accessUrlPatternList is empty for ClientId[{}]", clientId);
			return false;
		}
		List<String> urlPatterns = accessUrlPatternList.getPatternList(HttpMethod.resolve(request.getMethod()));
		if (CollectionUtils.isNotEmpty(urlPatterns)) {
			for (String eachPattern : urlPatterns) {
				RequestMatcher matcher = new AntPathRequestMatcher(eachPattern);
				if (matcher.matches(request)) {
					return true;
				}
			}
		}
		log.info("accessUrlPatternList do not match for ClientId[{}] with requestURI[{}]", clientId, request.getRequestURI());
		
		return false;
	}

	/**
	 * Represents each registered client.
	 */
	@Data
	public static class AuthorizedClientDetail {

		private String applicationCode;
		private String knowbilityUserId;
		private String knowbilityCredential;
		private String clientId;
		private String clientName;
		private AccessList accessList;

	}

	/**
	 * Represents allowed access list per each HTTP method(GET, POST, PUT, PATCH, and DELETE).
	 */
	@Data
	private static class AccessList {

		private List<String> get = emptyList();
		private List<String> post = emptyList();
		private List<String> put = emptyList();
		private List<String> patch = emptyList();
		private List<String> delete = emptyList();

		public List<String> getPatternList(HttpMethod httpMethod) {
			Map<HttpMethod, List<String>> methodMap = Map.of(GET, get, POST, post, PUT, put, PATCH, patch, DELETE, delete);
			return methodMap.getOrDefault(httpMethod, emptyList());
		}

		@Override
		public String toString() {
			return "AccessList [HTTP GET=" + get + ", HTTP POST=" + post + ", HTTP PUT=" + put + ", HTTP PATCH=" + patch + ", HTTP DELETE=" + delete + "]";
		}
	}

}