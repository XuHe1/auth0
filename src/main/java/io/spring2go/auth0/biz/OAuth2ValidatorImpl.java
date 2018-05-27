package io.spring2go.auth0.biz;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Implementation of {@link OAuth2Validator}
 */
import io.spring2go.auth0.model.AccessTokenRequest;
import io.spring2go.auth0.model.AuthorizationRequest;
import io.spring2go.auth0.model.Client;
import io.spring2go.auth0.service.ClientService;

import static io.spring2go.auth0.biz.OAuth2Validator.ValidationResponse.*;

@Component
public class OAuth2ValidatorImpl implements OAuth2Validator {

	private static final Set<String> RESPONSE_TYPES = new HashSet<String>();
	private static final Set<String> GRANT_TYPES = new HashSet<String>();

	static {
		RESPONSE_TYPES.add(IMPLICIT_GRANT_RESPONSE_TYPE);
		RESPONSE_TYPES.add(AUTHORIZATION_CODE_GRANT_RESPONSE_TYPE);

		GRANT_TYPES.add(GRANT_TYPE_AUTHORIZATION_CODE);
		GRANT_TYPES.add(GRANT_TYPE_REFRESH_TOKEN);
		GRANT_TYPES.add(GRANT_TYPE_CLIENT_CREDENTIALS);
	}

	@Autowired
	private ClientService clientService;

	@Override
	public ValidationResponse validate(AuthorizationRequest authorizationRequest) {
		try {
			validateAuthorizationRequest(authorizationRequest);

			String responseType = validateResponseType(authorizationRequest);

			Client client = validateClient(authorizationRequest);
			authorizationRequest.setClient(client);
			authorizationRequest.setClientId(client.getId());

			String redirectUri = determineRedirectUri(authorizationRequest, responseType, client);
			authorizationRequest.setRedirectUri(redirectUri);

			List<String> scopes = determineScopes(authorizationRequest, client);
			authorizationRequest.setRequestedScopes(String.join(",", scopes));

		} catch (ValidationResponseException e) {
			return e.v;
		}
		return VALID;
	}

	@Override
	public ValidationResponse validate(AccessTokenRequest request) {
		try {
			validateGrantType(request);

			validateAttributes(request);

			validateAccessTokenRequest(request);

		} catch (ValidationResponseException e) {
			return e.v;
		}
		return VALID;
	}

	protected List<String> determineScopes(AuthorizationRequest authorizationRequest, Client client) {
		if (StringUtils.isBlank(authorizationRequest.getRequestedScopes())) {
			// TODO add default scopes.
			return null;
		} else {
			List<String> scopes = Arrays.asList(authorizationRequest.getRequestedScopes().split("\\s*,\\s*"));
			String clientScopes = client.getScopes();
			for (String scope : scopes) {
				if (clientScopes.indexOf(scope) < 0) {
					throw new ValidationResponseException(SCOPE_NOT_VALID);
				}
			}
			return scopes;
		}
	}

	protected String determineRedirectUri(AuthorizationRequest authorizationRequest, String responseType,
			Client client) {
		List<String> uris = Arrays.asList(client.getRedirectUris().split("\\s*,\\s*"));
		String redirectUri = authorizationRequest.getRedirectUri();
		if (StringUtils.isBlank(redirectUri)) {
			if (responseType.equals(IMPLICIT_GRANT_RESPONSE_TYPE)) {
				throw new ValidationResponseException(IMPLICIT_GRANT_REDIRECT_URI);
			} else if (CollectionUtils.isEmpty(uris)) {
				throw new ValidationResponseException(REDIRECT_URI_REQUIRED);
			} else {
				return uris.get(0);
			}
		} else if (!AuthenticationFilter.isValidUrl(redirectUri)) {
			throw new ValidationResponseException(REDIRECT_URI_NOT_URI);
		} else if (redirectUri.contains("#")) {
			throw new ValidationResponseException(REDIRECT_URI_FRAGMENT_COMPONENT);
		} else if (!CollectionUtils.isEmpty(uris)) {
			boolean match = false;
			for (String uri : uris) {
				if (redirectUri.startsWith(uri)) {
					match = true;
					break;
				}
			}
			if (!match) {
				// Reset the redirect uri to first of the registered ones. Otherwise the result
				// error response would be undesired: a (possibly on purpose) redirect to URI
				// that is not acked.
				authorizationRequest.setRedirectUri(uris.get(0));
				throw new ValidationResponseException(REDIRECT_URI_NOT_VALID);
			}
		}
		return redirectUri;
	}

	protected Client validateClient(AuthorizationRequest authorizationRequest) {
		String clientId = authorizationRequest.getUniqueClientId();
		Client client = clientId == null ? null : clientService.findByUniqueClientId(clientId);
		if (client == null) {
			throw new ValidationResponseException(UNKNOWN_CLIENT_ID);
		}
		if (!client.isAllowedImplicitGrant()
				&& authorizationRequest.getResponseType().equals(IMPLICIT_GRANT_RESPONSE_TYPE)) {
			throw new ValidationResponseException(IMPLICIT_GRANT_NOT_PERMITTED);
		}
		return client;
	}

	protected String validateResponseType(AuthorizationRequest authorizationRequest) {
		String responseType = authorizationRequest.getResponseType();
		if (StringUtils.isBlank(responseType) || !RESPONSE_TYPES.contains(responseType)) {
			throw new ValidationResponseException(UNSUPPORTED_RESPONSE_TYPE);
		}
		return responseType;
	}

	protected void validateAuthorizationRequest(AuthorizationRequest authorizationRequest) {
	}

	protected void validateGrantType(AccessTokenRequest request) {
		String grantType = request.getGrantType();
		if (StringUtils.isBlank(grantType) || !GRANT_TYPES.contains(grantType)) {
			throw new ValidationResponseException(UNSUPPORTED_GRANT_TYPE);
		}
	}

	protected void validateAttributes(AccessTokenRequest request) {
		String grantType = request.getGrantType();
		if (GRANT_TYPE_AUTHORIZATION_CODE.equals(grantType)) {
			if (StringUtils.isBlank(request.getCode())) {
				throw new ValidationResponseException(INVALID_GRANT_AUTHORIZATION_CODE);
			}
		} else if (GRANT_TYPE_REFRESH_TOKEN.equals(grantType)) {
			if (StringUtils.isBlank(request.getRefreshToken())) {
				throw new ValidationResponseException(INVALID_GRANT_REFRESH_TOKEN);
			}
		}
	}

	protected void validateAccessTokenRequest(AccessTokenRequest accessTokenRequest) {
		if (accessTokenRequest.getGrantType().equals(GRANT_TYPE_CLIENT_CREDENTIALS)) {
			String clientId = accessTokenRequest.getUniqueClientId();
			Client client = StringUtils.isBlank(clientId) ? null : clientService.findByUniqueClientId(clientId);
			if (client == null) {
				throw new ValidationResponseException(UNKNOWN_CLIENT_ID);
			}
			if (!client.isAllowedClientCredentials()) {
				throw new ValidationResponseException(CLIENT_CREDENTIALS_NOT_PERMITTED);
			}
			accessTokenRequest.setClient(client);
		}

	}

}
