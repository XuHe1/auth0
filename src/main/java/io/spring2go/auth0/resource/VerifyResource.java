package io.spring2go.auth0.resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.alibaba.druid.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.spring2go.auth0.biz.ObjectMapperProvider;
import io.spring2go.auth0.model.AccessToken;
import io.spring2go.auth0.model.Client;
import io.spring2go.auth0.model.VerifyTokenResponse;
import io.spring2go.auth0.principal.AuthenticatedPrincipal;
import io.spring2go.auth0.principal.UserPassCredentials;
import io.spring2go.auth0.service.AccessTokenService;
import io.spring2go.auth0.service.ClientService;

/**
 * Resource for handling the call from resource servers to validate an access
 * token. As this is not part of the oauth2
 * <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2">spec</a>, we have
 * taken the Google <a href=
 * "https://developers.google.com/accounts/docs/OAuth2Login#validatingtoken"
 * >specification</a> as basis.
 */
@Path("/v1/tokeninfo")
@Produces(MediaType.APPLICATION_JSON)
@Component
public class VerifyResource implements EnvironmentAware {

	private static final Logger LOG = LoggerFactory.getLogger(VerifyResource.class);

	private static final ObjectMapper mapper = new ObjectMapperProvider().getContext(ObjectMapper.class);

	@Autowired
	private AccessTokenService accessTokenService;

	@Autowired
	private ClientService clientService;

	private boolean jsonTypeInfoIncluded;

	private boolean tokenExpired(AccessToken token) {
		return token.getExpires() != 0 && token.getExpires() < System.currentTimeMillis();
	}

	@GET
	public Response verifyToken(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
			@QueryParam("access_token") String accessToken) throws IOException {

		UserPassCredentials credentials = new UserPassCredentials(authorization);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Incoming verify-token request, access token: {}, credentials from authorization header: {}",
					accessToken, credentials);
		}

		// TODO 
		// 高频访问，考虑性能暂忽略验证
		// 实际需要根据场景看是否增加验证

		AccessToken token = accessTokenService.findByToken(accessToken);
	    if (token == null) {
	        LOG.warn("Access token {} not found. Responding with 404 in VerifyResource#verifyToken for user {}", accessToken, credentials);
	        return Response.status(Status.NOT_FOUND).entity(new VerifyTokenResponse("not_found")).build();
	      }
		if (tokenExpired(token)) {
			LOG.warn("Token {} is expired. Responding with 410 in VerifyResource#verifyToken for user {}", accessToken,
					credentials);
			return Response.status(Status.GONE).entity(new VerifyTokenResponse("token_expired")).build();
		}

		
		// TODO
		// 优化消除client的DB访问
		Client client = clientService.findById(token.getClientId());
		AuthenticatedPrincipal principal = StringUtils.isEmpty(token.getEncodedPrincipal()) ? null
				: AuthenticatedPrincipal.deserialize(token.getEncodedPrincipal());
		List<String> scopes = StringUtils.isEmpty(token.getScopes()) ? null : Arrays.asList(token.getScopes().split(","));

		final VerifyTokenResponse verifyTokenResponse = new VerifyTokenResponse(client.getClientName(),
				scopes, principal, token.getExpires());

		if (LOG.isDebugEnabled()) {
			LOG.debug("Responding with 200 in VerifyResource#verifyToken for access token {} and user {}", accessToken,
					credentials);
		}
		return Response.ok(mapper.writeValueAsString(verifyTokenResponse)).build();
	}

//	protected Response unauthorized() {
//		return Response.status(Status.UNAUTHORIZED).header(WWW_AUTHENTICATE, BASIC_REALM).build();
//	}

	@Override
	public void setEnvironment(Environment environment) {
		jsonTypeInfoIncluded = Boolean.valueOf(environment.getProperty("adminService.jsonTypeInfoIncluded", "false"));
		if (jsonTypeInfoIncluded) {
			mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		} else {
			mapper.disableDefaultTyping();
		}
	}

	public boolean isJsonTypeInfoIncluded() {
		return jsonTypeInfoIncluded;
	}
}
