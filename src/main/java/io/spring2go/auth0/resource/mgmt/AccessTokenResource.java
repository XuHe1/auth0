package io.spring2go.auth0.resource.mgmt;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.spring2go.auth0.model.AccessToken;
import io.spring2go.auth0.service.AccessTokenService;

/**
 * JAX-RS Resource for maintaining owns access tokens.
 */
@Path("/admin/accessToken")
@Produces(MediaType.APPLICATION_JSON)
@Component
public class AccessTokenResource extends AbstractResource {

	private static final Logger LOG = LoggerFactory.getLogger(AccessTokenResource.class);

	@Autowired
	private AccessTokenService accessTokenService;

	/**
	 * Get all access token for the provided credentials (== owner).
	 */
	@GET
	public Response getAll(@Context HttpServletRequest request) {
		Response validateScopeResponse = validateScope(request, Collections.singletonList(AbstractResource.SCOPE_READ));
		if (validateScopeResponse != null) {
			return validateScopeResponse;
		}
		List<AccessToken> tokens = getAllAccessTokens(request);
		return Response.ok(tokens).build();
	}

	/**
	 * Get one token.
	 */
	@GET
	@Path("/{accessTokenId}")
	public Response getById(@Context HttpServletRequest request, @PathParam("accessTokenId") Long id) {
		Response validateScopeResponse = validateScope(request, Collections.singletonList(AbstractResource.SCOPE_READ));
		if (validateScopeResponse != null) {
			return validateScopeResponse;
		}
		return response(getAccessToken(request, id));
	}

	/**
	 * Delete an existing access token.
	 */
	@DELETE
	@Path("/{accessTokenId}")
	public Response delete(@Context HttpServletRequest request, @PathParam("accessTokenId") Long id) {
		Response validateScopeResponse = validateScope(request,
				Collections.singletonList(AbstractResource.SCOPE_WRITE));
		if (validateScopeResponse != null) {
			return validateScopeResponse;
		}
		AccessToken accessToken = getAccessToken(request, id);
		if (accessToken == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		LOG.debug("About to delete accessToken {}", id);
		accessTokenService.deleteById(id);
		return Response.noContent().build();
	}

	private AccessToken getAccessToken(HttpServletRequest request, Long id) {
		AccessToken accessToken;
		if (isAdminPrincipal(request)) {
			accessToken = accessTokenService.findById(id);
		} else {
			String owner = getUserId(request);
			accessToken = accessTokenService.findByIdAndResourceOwnerId(id, owner);
		}
		LOG.debug("About to return one accessToken with id {}: {}", id, accessToken);
		return accessToken;
	}

	private List<AccessToken> getAllAccessTokens(HttpServletRequest request) {
		List<AccessToken> accessTokens;
		if (isAdminPrincipal(request)) {
			accessTokens = accessTokenService.findAll();
			LOG.debug("About to return all access tokens ({}) for adminPrincipal", accessTokens.size());
		} else {
			String owner = getUserId(request);
			accessTokens = accessTokenService.findByResourceOwnerId(owner);
			LOG.debug("About to return all access tokens ({}) for owner {}", accessTokens.size(), owner);
		}
		return accessTokens;
	}

}
