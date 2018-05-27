package io.spring2go.auth0.principal;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

/**
 * Holder and parser for the username and password from the authentication
 * header.
 */
public class UserPassCredentials {
	private static final char SEMI_COLON = ':';
	private static final int BASIC_AUTH_PREFIX_LENGTH = "Basic ".length();

	private String username;
	private String password;

	/**
	 * Parse the username and password from the authorization header. If the
	 * username and password cannot be found they are set to null.
	 * 
	 * @param authorizationHeader
	 *            the authorization header
	 */
	public UserPassCredentials(final String authorizationHeader) {
		if (authorizationHeader == null || authorizationHeader.length() < BASIC_AUTH_PREFIX_LENGTH) {
			noValidAuthHeader();
			return;
		}

		String authPart = authorizationHeader.substring(BASIC_AUTH_PREFIX_LENGTH);
		String userpass = new String(Base64.decodeBase64(authPart.getBytes()));
		int index = userpass.indexOf(SEMI_COLON);
		if (index < 1) {
			noValidAuthHeader();
			return;
		}
		username = userpass.substring(0, index);
		password = userpass.substring(index + 1);
	}

	public UserPassCredentials(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	private void noValidAuthHeader() {
		username = null;
		password = null;
	}

	public boolean isValid() {
		return !StringUtils.isBlank(username) && !StringUtils.isBlank(password);
	}

	/**
	 * Get the username.
	 * 
	 * @return the username or null if the username was not found
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Get the password.
	 * 
	 * @return the password or null if the password was not found
	 */
	public String getPassword() {
		return password;
	}

	@Override
	public String toString() {
		return "UserPassCredentials [username=" + username + "]";
	}

	public String getAuthorizationHeaderValue() {
		String result = null;
		if (!StringUtils.isBlank(username) && !StringUtils.isBlank(password)) {
			String value = username + ":" + password;
			result = "Basic " + new String(Base64.encodeBase64(value.getBytes()));
		}
		return result;
	}

}
