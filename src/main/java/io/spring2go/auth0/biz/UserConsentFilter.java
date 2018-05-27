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

import org.apache.commons.lang3.StringUtils;

import io.spring2go.auth0.consent.FormUserConsentHandler;
import io.spring2go.auth0.model.AuthorizationRequest;
import io.spring2go.auth0.model.Client;
import io.spring2go.auth0.principal.AuthenticatedPrincipal;
import io.spring2go.auth0.service.AuthorizationRequestService;
import io.spring2go.auth0.service.ClientService;

/**
 *
 * {@link Filter} that ensures the Resource Owner grants consent for the use of
 * the Resource Server data to the Client app.
 *
 */
public class UserConsentFilter extends AuthorizationSupport implements Filter {

	private static final String RETURN_URI = "/oauth2/consent";

	private ClientService clientService;
	private AuthorizationRequestService authorizationRequestService;
	private AbstractUserConsentHandler userConsentHandler;
	
	public UserConsentFilter(AuthorizationRequestService authorizationRequestService,
			ClientService clientService,
			FormUserConsentHandler userConsentHandler) {
		this.authorizationRequestService = authorizationRequestService;
		this.clientService = clientService;
		this.userConsentHandler = userConsentHandler;
	}

	private AuthorizationRequest findAuthorizationRequest(HttpServletRequest request) {
		String authState = (String) request.getAttribute(AbstractAuthenticator.AUTH_STATE);
		if (StringUtils.isBlank(authState)) {
			authState = request.getParameter(AbstractAuthenticator.AUTH_STATE);
		}
		return authorizationRequestService.findByAuthState(authState);
	}

	private void storePrincipal(HttpServletRequest request, HttpServletResponse response,
			AuthorizationRequest authorizationRequest) throws IOException {
		AuthenticatedPrincipal principal = (AuthenticatedPrincipal) request
				.getAttribute(AbstractAuthenticator.PRINCIPAL);
		if (principal == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"No valid AbstractAuthenticator.PRINCIPAL on the Request");
			return;
		}
		authorizationRequest.setEncodedPrincipal(principal.serialize());
		authorizationRequestService.save(authorizationRequest);
	}

	private boolean initialRequest(HttpServletRequest request) {
		return (AuthenticatedPrincipal) request.getAttribute(AbstractAuthenticator.PRINCIPAL) != null;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		AuthorizationRequest authorizationRequest = findAuthorizationRequest(request);
		if (authorizationRequest == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"No valid AbstractAuthenticator.AUTH_STATE on the Request");
		} else {

			String clientId = authorizationRequest.getUniqueClientId();
			Client client = clientService.findByUniqueClientId(clientId);

			if (initialRequest(request)) {
				storePrincipal(request, response, authorizationRequest);
				request.setAttribute(AbstractAuthenticator.RETURN_URI, RETURN_URI);

				request.setAttribute(AbstractUserConsentHandler.CLIENT, client);
				if (!client.isSkipConsent()) {
					userConsentHandler.handleUserConsent(request, response, chain, getAuthStateValue(request),
							getReturnUri(request), client);
				} else {
					chain.doFilter(request, response);
				}
			} else {
				/*
				 * Ok, the consentHandler wants to have control again (because he stepped out)
				 */
				userConsentHandler.handleUserConsent(request, response, chain, getAuthStateValue(request),
						getReturnUri(request), client);
			}
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
