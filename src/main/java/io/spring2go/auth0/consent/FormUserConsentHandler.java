package io.spring2go.auth0.consent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.druid.util.StringUtils;

import io.spring2go.auth0.biz.AbstractAuthenticator;
import io.spring2go.auth0.biz.AbstractUserConsentHandler;
import io.spring2go.auth0.model.AccessToken;
import io.spring2go.auth0.model.AuthorizationRequest;
import io.spring2go.auth0.model.Client;
import io.spring2go.auth0.principal.AuthenticatedPrincipal;
import io.spring2go.auth0.service.AccessTokenService;
import io.spring2go.auth0.service.AuthorizationRequestService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Example {@link AbstractUserConsentHandler} that forwards to a form.
 * 
 */
@Component
public class FormUserConsentHandler extends AbstractUserConsentHandler {

	private static final String USER_OAUTH_APPROVAL = "user_oauth_approval";

	@Autowired
	private AccessTokenService accessTokenService;

	@Autowired
	private AuthorizationRequestService authorizationRequestService;

	@Override
	public void handleUserConsent(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			String authStateValue, String returnUri, Client client) throws IOException, ServletException {
		if (isUserConsentPost(request)) {
			if (processForm(request, response)) {
				chain.doFilter(request, response);
			}
		} else {
			processInitial(request, response, chain, returnUri, authStateValue, client);
		}
	}

	private boolean isUserConsentPost(HttpServletRequest request) {
		String oauthApproval = request.getParameter(USER_OAUTH_APPROVAL);
		return request.getMethod().equals(HttpMethod.POST.toString()) && !StringUtils.isEmpty(oauthApproval);
	}

	private void processInitial(HttpServletRequest request, ServletResponse response, FilterChain chain,
			String returnUri, String authStateValue, Client client) throws IOException, ServletException {
		AuthenticatedPrincipal principal = (AuthenticatedPrincipal) request
				.getAttribute(AbstractAuthenticator.PRINCIPAL);
		List<AccessToken> tokens = accessTokenService.findByResourceOwnerIdAndClientId(principal.getName(),
				client.getId());
		if (!CollectionUtils.isEmpty(tokens)) {
			// If another token is already present for this resource owner and client, no
			// new consent should be requested
			String[] grantedScopes = tokens.get(0).getScopes().split(","); // take the scopes of the first access token
																			// found.
			setGrantedScopes(request, grantedScopes);
			chain.doFilter(request, response);
		} else {
			AuthorizationRequest authorizationRequest = authorizationRequestService.findByAuthState(authStateValue);
			request.setAttribute("requestedScopes", authorizationRequest.getRequestedScopes());
			request.setAttribute("client", client);
			request.setAttribute(AUTH_STATE, authStateValue);
			request.setAttribute("actionUri", returnUri);
			((HttpServletResponse) response).setHeader("X-Frame-Options", "SAMEORIGIN");
			request.getRequestDispatcher(getUserConsentUrl()).forward(request, response);
		}

	}

	protected String getUserConsentUrl() {
		return "/userconsent.html";
	}

	private boolean processForm(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		if (Boolean.valueOf(request.getParameter(USER_OAUTH_APPROVAL))) {
			setAuthStateValue(request, request.getParameter(AUTH_STATE));
			String[] scopes = request.getParameterValues(GRANTED_SCOPES);
			setGrantedScopes(request, scopes);
			return true;
		} else {
			request.getRequestDispatcher(getUserConsentDeniedUrl()).forward(request, response);
			return false;
		}
	}

	protected String getUserConsentDeniedUrl() {
		return "/userconsent-denied.html";
	}

}
