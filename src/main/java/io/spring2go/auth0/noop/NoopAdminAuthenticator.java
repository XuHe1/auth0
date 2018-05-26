package io.spring2go.auth0.noop;

import io.spring2go.auth0.principal.AuthenticatedPrincipal;

/**
 * Grants isAdmin authority to the Principal
 */
public class NoopAdminAuthenticator extends NoopAuthenticator {
	@Override
	protected AuthenticatedPrincipal getAuthenticatedPrincipal() {
		AuthenticatedPrincipal principal = super.getAuthenticatedPrincipal();
		principal.setAdminPrincipal(true);
		return principal;
	}
}
