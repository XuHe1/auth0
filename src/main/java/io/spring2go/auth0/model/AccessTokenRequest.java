package io.spring2go.auth0.model;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representation of the AccessToken request defined in the <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2#page-27">spec</a>
 *
 */
public class AccessTokenRequest {
	@JsonProperty("grant_type")
	private String grantType;

	private String code;

	@JsonProperty("redirect_uri")
	private String redirectUri;

	@JsonProperty("client_id")
	private String clientId;

	@JsonProperty("client_secret")
	private String clientSecret;

	@JsonProperty("refresh_token")
	private String refreshToken;

	@JsonProperty("scope")
	private String scope;

	@JsonIgnore
	private Client client;

	public static AccessTokenRequest fromMultiValuedFormParameters(MultivaluedMap<String, String> formParameters) {
		AccessTokenRequest atr = new AccessTokenRequest();
		atr.setUniqueClientId(nullSafeGetFormParameter("client_id", formParameters));
		atr.setClientSecret(nullSafeGetFormParameter("client_secret", formParameters));
		atr.setCode(nullSafeGetFormParameter("code", formParameters));
		atr.setGrantType(nullSafeGetFormParameter("grant_type", formParameters));
		atr.setRedirectUri(nullSafeGetFormParameter("redirect_uri", formParameters));
		atr.setRefreshToken(nullSafeGetFormParameter("refresh_token", formParameters));
		atr.setScope(nullSafeGetFormParameter("scope", formParameters));
		return atr;
	}

	private static String nullSafeGetFormParameter(String parameterName, MultivaluedMap<String, String> formParameters) {
		List<String> params = formParameters.get(parameterName);
		return CollectionUtils.isEmpty(params) ? null : params.get(0);
	}

	/**
	 * @return the grantType
	 */
	public String getGrantType() {
		return grantType;
	}

	/**
	 * @param grantType
	 *            the grantType to set
	 */
	public void setGrantType(String grantType) {
		this.grantType = grantType;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the redirectUri
	 */
	public String getRedirectUri() {
		return redirectUri;
	}

	/**
	 * @param redirectUri
	 *            the redirectUri to set
	 */
	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	/**
	 * @return the clientId
	 */
	public String getUniqueClientId() {
		return clientId;
	}

	/**
	 * @param clientId
	 *            the clientId to set
	 */
	public void setUniqueClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the clientSecret
	 */
	public String getClientSecret() {
		return clientSecret;
	}

	/**
	 * @param clientSecret
	 *            the clientSecret to set
	 */
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	/**
	 * @return the refreshToken
	 */
	public String getRefreshToken() {
		return refreshToken;
	}

	/**
	 * @param refreshToken
	 *            the refreshToken to set
	 */
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	/**
	 * @return the scope
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * @param scope
	 *            the scope to set
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

}
