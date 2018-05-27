package io.spring2go.auth0.resource.mgmt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.spring2go.auth0.biz.OAuth2Validator;
import io.spring2go.auth0.model.ErrorResponse;
import io.spring2go.auth0.model.ValidationErrorResponse;
import io.spring2go.auth0.model.VerifyTokenResponse;
import io.spring2go.auth0.principal.AuthenticatedPrincipal;

import static io.spring2go.auth0.biz.AuthorizationServerFilter.VERIFY_TOKEN_RESPONSE;;

/**
 * Abstract resource that defines common functionality.
 */
public class AbstractResource {

	public static final String SCOPE_READ = "read";
	public static final String SCOPE_WRITE = "write";

	private static final Logger LOG = LoggerFactory.getLogger(AbstractResource.class);

	public Response buildErrorResponse(Exception e) {

		// TODO refinement

		Response.Status s = Response.Status.INTERNAL_SERVER_ERROR;
		String reason = "Internal server error";
		LOG.info("Responding with error '" + s + "', '" + reason + "'. Cause attached.", e);
		return Response.status(s).entity(reason).build();
	}

	protected String getUserId(HttpServletRequest request) {
		return getAuthenticatedPrincipal(request).getName();
	}

	protected boolean isAdminPrincipal(HttpServletRequest request) {
		return getAuthenticatedPrincipal(request).isAdminPrincipal();
	}

	private AuthenticatedPrincipal getAuthenticatedPrincipal(HttpServletRequest request) {
		VerifyTokenResponse verifyTokenResponse = (VerifyTokenResponse) request.getAttribute(VERIFY_TOKEN_RESPONSE);
		return verifyTokenResponse.getPrincipal();
	}

	public Response validateScope(HttpServletRequest request, List<String> requiredScopes) {
		VerifyTokenResponse verifyTokenResponse = (VerifyTokenResponse) request.getAttribute(VERIFY_TOKEN_RESPONSE);
		List<String> grantedScopes = verifyTokenResponse.getScopes();
		for (String requiredScope : requiredScopes) {
			if (!grantedScopes.contains(requiredScope)) {
				LOG.debug("Resource required scopes ({}) which the client has not been granted ({})", requiredScopes,
						grantedScopes);
				return Response.status(HttpServletResponse.SC_BAD_REQUEST)
						.entity(new ErrorResponse(OAuth2Validator.ValidationResponse.SCOPE_NOT_VALID.getValue(),
								OAuth2Validator.ValidationResponse.SCOPE_NOT_VALID.getDescription()))
						.build();
			}
		}
		return null;
	}
	
	protected Response buildViolationErrorResponse(List<String> violations) {
		ValidationErrorResponse responseBody = new ValidationErrorResponse(violations);
		return Response.status(Response.Status.BAD_REQUEST).entity(responseBody).build();
	}

	public String generateRandom() {
		return UUID.randomUUID().toString();
	}

	protected <T> List<T> addAll(Iterator<T> iterator) {
		List<T> result = new ArrayList<T>();
		while (iterator.hasNext()) {
			result.add(iterator.next());
		}
		return result;
	}

	protected Response response(Object response) {
		Response.ResponseBuilder responseBuilder = (response == null ? Response.status(Response.Status.NOT_FOUND)
				: Response.ok(response));
		return responseBuilder.build();
	}

}
