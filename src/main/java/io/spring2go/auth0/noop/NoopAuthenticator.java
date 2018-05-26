package io.spring2go.auth0.noop;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.spring2go.auth0.biz.AbstractAuthenticator;
import io.spring2go.auth0.principal.AuthenticatedPrincipal;

/**
 * A minimalistic implementation of AbstractAuthenticator that contains no
 * authentication but only fulfills the contract of Authenticators. Useful for
 * testing and demonstration purposes only, of course not safe for production.
 */
public class NoopAuthenticator extends AbstractAuthenticator {

	@Override
	public boolean canCommence(HttpServletRequest request) {
		return getAuthStateValue(request) != null;
	}

	@Override
	public void authenticate(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			String authStateValue, String returnUri) throws IOException, ServletException {
		super.setAuthStateValue(request, authStateValue);
		AuthenticatedPrincipal principal = getAuthenticatedPrincipal();
		super.setPrincipal(request, principal);
		chain.doFilter(request, response);
	}

	protected AuthenticatedPrincipal getAuthenticatedPrincipal() {
		return new AuthenticatedPrincipal("noop");
	}

}
