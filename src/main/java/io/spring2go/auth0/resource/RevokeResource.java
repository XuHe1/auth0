package io.spring2go.auth0.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.spring2go.auth0.biz.OAuth2Validator.ValidationResponse;
import io.spring2go.auth0.biz.ValidationResponseException;
import io.spring2go.auth0.model.AccessToken;
import io.spring2go.auth0.model.AccessTokenRequest;
import io.spring2go.auth0.model.Client;
import io.spring2go.auth0.model.ErrorResponse;
import io.spring2go.auth0.principal.UserPassCredentials;
import io.spring2go.auth0.service.AccessTokenService;
import io.spring2go.auth0.service.ClientService;

import static io.spring2go.auth0.biz.OAuth2Validator.ValidationResponse.UNKNOWN_CLIENT_ID;
import static io.spring2go.auth0.resource.TokenResource.BASIC_REALM;
import static io.spring2go.auth0.resource.TokenResource.WWW_AUTHENTICATE;

import java.util.List;

/**
 * Resource for handling the call to revoke an access token, as described in RFC
 * 7009. http://tools.ietf.org/html/rfc7009
 *
 */
@Path("/v1/revoke")
@Produces(MediaType.APPLICATION_JSON)
@Consumes("application/x-www-form-urlencoded")
@Component
public class RevokeResource {

	private static final Logger LOG = LoggerFactory.getLogger(RevokeResource.class);

	@Autowired
	private AccessTokenService accessTokenService;

	@Autowired
	private ClientService clientService;

	@POST
	public Response revokeAccessToken(@HeaderParam("Authorization") String authorization,
			final MultivaluedMap<String, String> formParameters) {
		String accessToken;
		Client client;
		AccessTokenRequest accessTokenRequest = AccessTokenRequest.fromMultiValuedFormParameters(formParameters);
		UserPassCredentials credentials = getClientCredentials(authorization, accessTokenRequest);
		try {
			client = validateClient(credentials);
			if (!client.isExactMatch(credentials)) {
				return Response.status(Status.UNAUTHORIZED).header(WWW_AUTHENTICATE, BASIC_REALM).build();
			}
			List<String> params = formParameters.get("token");
			accessToken = CollectionUtils.isEmpty(params) ? null : params.get(0);
		} catch (ValidationResponseException e) {
			ValidationResponse validationResponse = e.v;
			return Response.status(Status.BAD_REQUEST)
					.entity(new ErrorResponse(validationResponse.getValue(), validationResponse.getDescription()))
					.build();
		}
		AccessToken token = accessTokenService.findByTokenAndClientId(accessToken, client.getId());
		if (token == null) {
			LOG.info("Access token {} not found for client '{}'. Will return OK however.", accessToken,
					client.getClientId());
			return Response.ok().build();
		}
		accessTokenService.delete(token);
		return Response.ok().build();
	}

	protected Client validateClient(UserPassCredentials credentials) {
		String clientId = credentials.getUsername();
		Client client = StringUtils.isEmpty(clientId) ? null : clientService.findByUniqueClientId(clientId);
		if (client == null) {
			throw new ValidationResponseException(UNKNOWN_CLIENT_ID);
		}
		return client;
	}

	private UserPassCredentials getClientCredentials(String authorization, AccessTokenRequest accessTokenRequest) {
		return StringUtils.isEmpty(authorization)
				? new UserPassCredentials(accessTokenRequest.getUniqueClientId(), accessTokenRequest.getClientSecret())
				: new UserPassCredentials(authorization);
	}
}
