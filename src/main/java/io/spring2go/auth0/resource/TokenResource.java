package io.spring2go.auth0.resource;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.spring2go.auth0.biz.AbstractAuthenticator;
import io.spring2go.auth0.biz.AbstractUserConsentHandler;
import io.spring2go.auth0.biz.OAuth2Validator;
import io.spring2go.auth0.biz.OAuth2Validator.ValidationResponse;
import io.spring2go.auth0.biz.ValidationResponseException;
import io.spring2go.auth0.model.AccessToken;
import io.spring2go.auth0.model.AccessTokenRequest;
import io.spring2go.auth0.model.AccessTokenResponse;
import io.spring2go.auth0.model.AuthorizationRequest;
import io.spring2go.auth0.model.Client;
import io.spring2go.auth0.model.ErrorResponse;
import io.spring2go.auth0.principal.AuthenticatedPrincipal;
import io.spring2go.auth0.principal.UserPassCredentials;
import io.spring2go.auth0.service.AccessTokenService;
import io.spring2go.auth0.service.AuthorizationRequestService;
import io.spring2go.auth0.service.ClientService;

import static io.spring2go.auth0.biz.OAuth2Validator.*;

/**
 * Resource for handling all calls related to tokens. It adheres to
 * <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2"> the OAuth spec</a>.
 *
 */
@Path("/oauth2")
@Component
public class TokenResource {
	public static final String BASIC_REALM = "Basic realm=\"OAuth2 Secure\"";

	public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

	@Autowired
	private AuthorizationRequestService authorizationRequestService;

	@Autowired
	private AccessTokenService accessTokenService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private OAuth2Validator oAuth2Validator;

	private static final Logger LOG = LoggerFactory.getLogger(TokenResource.class);

	@GET
	@Path("/authorize")
	public Response authorizeCallbackGet(@Context HttpServletRequest request) {
		return authorizeCallback(request);
	}

	/**
	 * Entry point for the authorize call which needs to return an authorization
	 * code or (implicit grant) an access token
	 *
	 * @param request
	 *            the {@link HttpServletRequest}
	 * @return Response the response
	 */
	@POST
	@Produces(MediaType.TEXT_HTML)
	@Path("/authorize")
	public Response authorizeCallback(@Context HttpServletRequest request) {
		return doProcess(request);
	}

	/**
	 * Called after the user has given consent
	 *
	 * @param request
	 *            the {@link HttpServletRequest}
	 * @return Response the response
	 */
	@POST
	@Produces(MediaType.TEXT_HTML)
	@Path("/consent")
	public Response consentCallback(@Context HttpServletRequest request) {
		return doProcess(request);
	}

	private Response doProcess(HttpServletRequest request) {
		AuthorizationRequest authReq = findAuthorizationRequest(request);
		if (authReq == null) {
			return serverError("Not a valid AbstractAuthenticator.AUTH_STATE on the Request");
		}
		// set client
		Client client = clientService.findById(authReq.getClientId());
		authReq.setClient(client);
		processScopes(authReq, request);
		if (authReq.getResponseType().equals(OAuth2Validator.IMPLICIT_GRANT_RESPONSE_TYPE)) {
			AccessToken token = createAccessToken(authReq, true);
			return sendImplicitGrantResponse(authReq, token);
		} else {
			return sendAuthorizationCodeResponse(authReq);
		}
	}

	@POST
	@Path("/token")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes("application/x-www-form-urlencoded")
	public Response token(@HeaderParam("Authorization") String authorization,
			final MultivaluedMap<String, String> formParameters) {
		AccessTokenRequest accessTokenRequest = AccessTokenRequest.fromMultiValuedFormParameters(formParameters);
		UserPassCredentials credentials = getUserPassCredentials(authorization, accessTokenRequest);
		String grantType = accessTokenRequest.getGrantType();
		if (GRANT_TYPE_CLIENT_CREDENTIALS.equals(grantType)) {
			accessTokenRequest.setUniqueClientId(credentials.getUsername());
		}
		ValidationResponse vr = oAuth2Validator.validate(accessTokenRequest);
		if (!vr.valid()) {
			return sendErrorResponse(vr);
		}
		AuthorizationRequest request;
		try {
			if (GRANT_TYPE_AUTHORIZATION_CODE.equals(grantType)) {
				request = authorizationCodeToken(accessTokenRequest);
			} else if (GRANT_TYPE_REFRESH_TOKEN.equals(grantType)) {
				request = refreshTokenToken(accessTokenRequest);
			} else if (GRANT_TYPE_CLIENT_CREDENTIALS.equals(grantType)) {
				request = new AuthorizationRequest();
				request.setClientId(accessTokenRequest.getClient().getId());
				// We have to construct a AuthenticatedPrincipal on-the-fly as there is only
				// key-secret authentication
				request.setEncodedPrincipal(
						new AuthenticatedPrincipal(accessTokenRequest.getClient().getClientId()).serialize());
				// Apply all client scopes to the access token.
				// TODO: take into account given scopes from the request
				request.setGrantedScopes(accessTokenRequest.getClient().getScopes());
			} else {
				return sendErrorResponse(ValidationResponse.UNSUPPORTED_GRANT_TYPE);
			}
		} catch (ValidationResponseException e) {
			return sendErrorResponse(e.v);
		}

		// populate client
		if (accessTokenRequest.getClient() != null) { // already set in oAuth2Validator.validate
			request.setClient(accessTokenRequest.getClient());
		} else {
			Client client = clientService.findById(request.getClientId());
			request.setClient(client);
		}

		if (!request.getClient().isExactMatch(credentials)) {
			return Response.status(Status.UNAUTHORIZED).header(WWW_AUTHENTICATE, BASIC_REALM).build();
		}
		AccessToken token = createAccessToken(request, false);

		AccessTokenResponse response = new AccessTokenResponse(token.getToken(), BEARER, token.getExpiresIn(),
				token.getRefreshToken(), token.getScopes());

		return Response.ok().entity(response).cacheControl(cacheControlNoStore()).header("Pragma", "no-cache").build();

	}

	public boolean validateClientCredentials(Client client, UserPassCredentials credentials) {
		return credentials != null && credentials.isValid() && credentials.getUsername().equals(client.getClientId())
				&& credentials.getPassword().equals(client.getSecret());
	}

	/*
	 * In the user consent filter the scopes are (possible) set on the Request
	 */
	private void processScopes(AuthorizationRequest authReq, HttpServletRequest request) {
		if (authReq.getClient().isSkipConsent()) {
			// return the scopes in the authentication request since the requested scopes
			// are stored in the authorizationRequest.
			authReq.setGrantedScopes(authReq.getRequestedScopes());
		} else {
			String[] scopes = (String[]) request.getAttribute(AbstractUserConsentHandler.GRANTED_SCOPES);
			if (!ArrayUtils.isEmpty(scopes)) {
				authReq.setGrantedScopes(String.join(",", scopes));
			} else {
				authReq.setGrantedScopes(null);
			}
		}
	}

	private AccessToken createAccessToken(AuthorizationRequest request, boolean isImplicitGrant) {
		Client client = request.getClient();
		long expireDuration = client.getExpireDuration();
		long expires = (expireDuration == 0L ? 0L : (System.currentTimeMillis() + (1000 * expireDuration)));
		String refreshToken = (client.isUseRefreshTokens() && !isImplicitGrant) ? getTokenValue(true) : null;
		AccessToken token = new AccessToken();
		token.setToken(getTokenValue(false));
		token.setEncodedPrincipal(request.getEncodedPrincipal());
		token.setClientId(client.getId());
		token.setExpires(expires);
		token.setScopes(request.getGrantedScopes());
		token.setRefreshToken(refreshToken);
		accessTokenService.save(token);
		return token;
	}

	private AuthorizationRequest findAuthorizationRequest(HttpServletRequest request) {
		String authState = (String) request.getAttribute(AbstractAuthenticator.AUTH_STATE);
		return authorizationRequestService.findByAuthState(authState);
	}

	private Response sendErrorResponse(String error, String description) {
		return Response.status(Status.BAD_REQUEST).entity(new ErrorResponse(error, description)).build();
	}

	private Response sendErrorResponse(ValidationResponse response) {
		return sendErrorResponse(response.getValue(), response.getDescription());
	}

	private Response sendImplicitGrantResponse(AuthorizationRequest authReq, AccessToken accessToken) {
		String uri = authReq.getRedirectUri();
		String fragment = String.format(
				"access_token=%s&token_type=bearer&expires_in=%s&scope=%s" + appendStateParameter(authReq),
				accessToken.getToken(), accessToken.getExpiresIn(), authReq.getGrantedScopes());
		if (authReq.getClient().isIncludePrincipal()) {
			AuthenticatedPrincipal ap = AuthenticatedPrincipal.deserialize(authReq.getEncodedPrincipal());
			fragment += String.format("&principal=%s", ap.getDisplayName());
		}
		return Response.seeOther(UriBuilder.fromUri(uri).fragment(fragment).build()).cacheControl(cacheControlNoStore())
				.header("Pragma", "no-cache").build();

	}

	private String appendStateParameter(AuthorizationRequest authReq) {
		String state = authReq.getState();
		try {
			return StringUtils.isBlank(state) ? "" : "&state=".concat(URLEncoder.encode(state, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private CacheControl cacheControlNoStore() {
		CacheControl cacheControl = new CacheControl();
		cacheControl.setNoStore(true);
		return cacheControl;
	}

	private AuthorizationRequest authorizationCodeToken(AccessTokenRequest accessTokenRequest) {
		AuthorizationRequest authReq = authorizationRequestService
				.findByAuthorizationCode(accessTokenRequest.getCode());
		if (authReq == null) {
			throw new ValidationResponseException(ValidationResponse.INVALID_GRANT_AUTHORIZATION_CODE);
		}
		String uri = accessTokenRequest.getRedirectUri();
		if (!authReq.getRedirectUri().equalsIgnoreCase(uri)) {
			throw new ValidationResponseException(ValidationResponse.REDIRECT_URI_DIFFERENT);
		}
		authorizationRequestService.deleteById(authReq.getId());
		return authReq;
	}

	private AuthorizationRequest refreshTokenToken(AccessTokenRequest accessTokenRequest) {
		AccessToken accessToken = accessTokenService.findByRefreshToken(accessTokenRequest.getRefreshToken());
		if (accessToken == null) {
			throw new ValidationResponseException(ValidationResponse.INVALID_GRANT_REFRESH_TOKEN);
		}
		AuthorizationRequest request = new AuthorizationRequest();
		request.setClientId(accessToken.getClientId());
		request.setEncodedPrincipal(accessToken.getEncodedPrincipal());
		request.setGrantedScopes(accessToken.getScopes());
		accessTokenService.delete(accessToken);
		return request;
	}

	/*
	 * http://tools.ietf.org/html/draft-ietf-oauth-v2#section-2.3.1
	 *
	 * We support both options. Clients can use the Basic Authentication or include
	 * the secret and id in the request body
	 */

	private UserPassCredentials getUserPassCredentials(String authorization, AccessTokenRequest accessTokenRequest) {
		return StringUtils.isBlank(authorization)
				? new UserPassCredentials(accessTokenRequest.getUniqueClientId(), accessTokenRequest.getClientSecret())
				: new UserPassCredentials(authorization);
	}

	private Response sendAuthorizationCodeResponse(AuthorizationRequest authReq) {
		String uri = authReq.getRedirectUri();
		String authorizationCode = getAuthorizationCodeValue();
		authReq.setAuthorizationCode(authorizationCode);
		authorizationRequestService.save(authReq);
		uri = uri + appendQueryMark(uri) + "code=" + authorizationCode + appendStateParameter(authReq);
		return Response.seeOther(UriBuilder.fromUri(uri).build()).cacheControl(cacheControlNoStore())
				.header("Pragma", "no-cache").build();
	}

	private String appendQueryMark(String uri) {
		return uri.contains("?") ? "&" : "?";
	}

	protected String getTokenValue(boolean isRefreshToken) {
		return UUID.randomUUID().toString();
	}

	protected String getAuthorizationCodeValue() {
		return getTokenValue(false);
	}

	private Response serverError(String msg) {
		LOG.warn(msg);
		return Response.serverError().build();
	}

}
