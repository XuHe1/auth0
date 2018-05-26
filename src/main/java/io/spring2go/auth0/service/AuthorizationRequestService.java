package io.spring2go.auth0.service;

import io.spring2go.auth0.core.Service;
import io.spring2go.auth0.model.AuthorizationRequest;

public interface AuthorizationRequestService extends Service<AuthorizationRequest> {

	public AuthorizationRequest findByAuthState(String authState);
	
	public AuthorizationRequest findByAuthorizationCode(String authorizationCode);
	
}
