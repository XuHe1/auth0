package io.spring2go.auth0.biz;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.spring2go.auth0.model.TokenResponseCache;
import io.spring2go.auth0.model.TokenResponseCacheImpl;
import io.spring2go.auth0.model.VerifyTokenResponse;

/**
 * {@link Filter} which can be used to protect all relevant resources by
 * validating the oauth access token with the Authorization server. This is an
 * example configuration:
 * <p/>
 * 
 * <pre>
 * {@code
 * <filter>
 *   <filter-name>authorization-server</filter-name>
 *   <filter-class>io.spring2go.auth0.biz.AuthorizationServerFilter</filter-class>
 *   <init-param>
 *     <param-name>authorization-server-url</param-name>
 *     <param-value>http://<host-name>/v1/tokeninfo</param-value>
 *   </init-param>
 *   <init-param>
 *     <param-name>type-information-is-included</param-name>
 *     <param-value>true</param-value>
 *   </init-param>
 * </filter>
 * <filter-mapping>
 *   <filter-name>authorization-server</filter-name>
 *  <url-pattern>/*</url-pattern>
 * </filter-mapping>
 * }
 * </pre>
 * <p/>
 * The response of the Authorization Server is put on the
 * {@link HttpServletRequest} with the name
 * {@link AuthorizationServerFilter#VERIFY_TOKEN_RESPONSE}.
 * <p/>
 * Of course it might be better to use a properties file depending on the
 * environment (e.g. OTAP) to get the name, secret and url. This can be achieved
 * simple to provide an apis.application.properties file on the classpath or
 * configure a properties file name as init-param (to have multiple resource
 * servers in the same tomcat instance).
 * <p/>
 * See {@link AuthorizationServerFilter#init(FilterConfig)}
 * <p/>
 * <p/>
 * Also note that by default the responses from the Authorization Server are not
 * cached. This in configurable in the properties file used by this Filter.
 * Again see {@link AuthorizationServerFilter#init(FilterConfig)}
 * <p/>
 * The cache behaviour can also be changed if you override
 * {@link AuthorizationServerFilter#cacheAccessTokens()} and to configure the
 * cache differently override {@link AuthorizationServerFilter#buildCache()}
 */
public class AuthorizationServerFilter implements Filter {

	private static final Logger LOG = LoggerFactory.getLogger(AuthorizationServerFilter.class);

	/*
	 * Endpoint of the authorization server (e.g. something like
	 * http://<host-name>/v1/tokeninfo)
	 */
	private String authorizationServerUrl;

	/*
	 * Base64-encoded concatenation of the name of the resource server and the
	 * secret separated with a colon
	 */
	// private String authorizationValue;

	/*
	 * Client to make GET calls to the authorization server
	 */
	private Client client;

	/*
	 * Constant for the access token (oauth2 spec)
	 */
	private static final String BEARER = "bearer";

	/*
	 * Constant name of the request attribute where the response is stored
	 */
	public static final String VERIFY_TOKEN_RESPONSE = "VERIFY_TOKEN_RESPONSE";

	/*
	 * If not overridden by a subclass / configured otherwise we don't cache the
	 * answers from the authorization server
	 */
	private boolean cacheEnabled = false;
	private TokenResponseCache cache;

	/*
	 * By default we respond to preflight CORS requests and have a lenient policy as
	 * we are secured by OAuth2
	 */
	private boolean allowCorsRequests = true;

	/**
	 * Whether (java) type information is included in the VerifyTokenResponse.
	 */
	private boolean typeInformationIsIncluded = false;

	private ObjectMapper objectMapper;

	public AuthorizationServerFilter(String authorizationServerUrl) {
		this.authorizationServerUrl = authorizationServerUrl;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		Assert.hasText(authorizationServerUrl, "Must provide a authorization server url");

		if (cacheAccessTokens()) {
			this.cache = buildCache();
			Assert.notNull(this.cache);
		}

		this.client = createClient();

		this.objectMapper = createObjectMapper(typeInformationIsIncluded);
	}

	protected ObjectMapper createObjectMapper(boolean typeInformationIsIncluded) {
		ObjectMapper mapper = new ObjectMapperProvider().getContext(ObjectMapper.class);
		if (typeInformationIsIncluded) {
			mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		} else {
			mapper.disableDefaultTyping();
		}
		return mapper;
	}

	/**
	 * @return Client
	 */
	protected Client createClient() {
		return JerseyClientBuilder.createClient();
		// TODO add ObjectMapperProvider.class?
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected TokenResponseCache buildCache() {
		return new TokenResponseCacheImpl(1000, 60 * 5);
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		if (handledCorsPreflightRequest(request, response)) {
			return;
		}
		/*
		 * The Access Token from the Client app as documented in
		 * http://tools.ietf.org/html/draft-ietf-oauth-v2#section-7
		 */
		final String accessToken = getAccessToken(request);
		if (accessToken != null) {
			VerifyTokenResponse tokenResponse = getVerifyTokenResponse(accessToken);
			if (isValidResponse(tokenResponse)) {
				request.setAttribute(VERIFY_TOKEN_RESPONSE, tokenResponse);
				chain.doFilter(request, response);
				return;
			}
		}
		sendError(response, HttpServletResponse.SC_FORBIDDEN, "OAuth2 endpoint");
	}

	protected VerifyTokenResponse getVerifyTokenResponse(String accessToken) {
		VerifyTokenResponse verifyTokenResponse = null;
		if (cacheAccessTokens()) {
			verifyTokenResponse = cache.getVerifyToken(accessToken);
			if (verifyTokenResponse != null) {
				return verifyTokenResponse;
			}
		}
		if (verifyTokenResponse == null) {
			ClientResponse res = client.target(String.format("%s?access_token=%s", authorizationServerUrl, accessToken))
					.request()
					// ignore authorization
					// .header(HttpHeaders.AUTHORIZATION, "Basic " + authorizationValue)
					.accept("application/json").get(ClientResponse.class);
			try {
				String responseString = res.readEntity(String.class);
				int statusCode = res.getStatus();
				LOG.debug("Got verify token response (status: {}): '{}'", statusCode, responseString);
				if (statusCode == HttpServletResponse.SC_OK) {
					verifyTokenResponse = objectMapper.readValue(responseString, VerifyTokenResponse.class);
				}
			} catch (Exception e) {
				LOG.error("Exception in reading result from AuthorizationServer", e);
				// anti-pattern, but null case is explicitly handled
			}
		}

		if (isValidResponse(verifyTokenResponse) && cacheAccessTokens()) {
			cache.storeVerifyToken(accessToken, verifyTokenResponse);
		}
		return verifyTokenResponse;
	}

	protected void sendError(HttpServletResponse response, int statusCode, String reason) {
		LOG.warn("No valid access-token on request. Will respond with error response: {} {}", statusCode, reason);
		try {
			response.sendError(statusCode, reason);
			response.flushBuffer();
		} catch (IOException e) {
			throw new RuntimeException(reason, e);
		}
	}

	protected boolean cacheAccessTokens() {
		return cacheEnabled;
	}

	/*
	 * http://www.w3.org/TR/cors/#resource-preflight-requests
	 */
	protected boolean handledCorsPreflightRequest(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (!this.allowCorsRequests || StringUtils.isBlank(request.getHeader("Origin"))) {
			return false;
		}
		/*
		 * We must do this anyway, this being (probably) a CORS request
		 */
		response.setHeader("Access-Control-Allow-Origin", "*");
		if (StringUtils.isNotBlank(request.getHeader("Access-Control-Request-Method"))
				&& request.getMethod().equalsIgnoreCase("OPTIONS")) {
			/*
			 * We don't want to propogate the request any further
			 */
			response.setHeader("Access-Control-Allow-Methods", getAccessControlAllowedMethods());
			String requestHeaders = request.getHeader("Access-Control-Request-Headers");
			if (StringUtils.isNotBlank(requestHeaders)) {
				response.setHeader("Access-Control-Allow-Headers", getAllowedHeaders(requestHeaders));
			}
			response.setHeader("Access-Control-Max-Age", getAccessControlMaxAge());
			response.setStatus(HttpServletResponse.SC_OK);
			response.flushBuffer();
			return true;
		}
		return false;
	}

	protected String getAllowedHeaders(String requestHeaders) {
		return requestHeaders;
	}

	protected String getAccessControlMaxAge() {
		return "86400";
	}

	protected String getAccessControlAllowedMethods() {
		return "GET, OPTIONS, HEAD, PUT, PATCH, POST, DELETE";
	}

	private boolean isValidResponse(VerifyTokenResponse tokenResponse) {
		return tokenResponse != null && tokenResponse.getPrincipal() != null && tokenResponse.getError() == null;
	}

	private String getAccessToken(HttpServletRequest request) {
		String accessToken = null;
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null) {
			int space = header.indexOf(' ');
			if (space > 0) {
				String method = header.substring(0, space);
				if (BEARER.equalsIgnoreCase(method)) {
					accessToken = header.substring(space + 1);
				}
			}
		}
		return accessToken;
	}

	@Override
	public void destroy() {
	}

	public void setCacheEnabled(boolean cacheEnabled) {
		this.cacheEnabled = cacheEnabled;
	}

	public void setAllowCorsRequests(boolean allowCorsRequests) {
		this.allowCorsRequests = allowCorsRequests;
	}

	public void setTypeInformationIsIncluded(boolean typeInformationIsIncluded) {
		this.typeInformationIsIncluded = typeInformationIsIncluded;
	}

}
