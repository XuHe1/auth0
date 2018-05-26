package io.spring2go.auth0.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.spring2go.auth0.core.AbstractService;
import io.spring2go.auth0.model.AuthorizationRequest;
import io.spring2go.auth0.service.AuthorizationRequestService;

/**
 * AuthorizationRequest Service Implementation
 */
@Service
@Transactional
public class AuthorizationRequestServiceImpl extends AbstractService<AuthorizationRequest> implements AuthorizationRequestService {

	@Override
	public AuthorizationRequest findByAuthState(String authState) {
		return super.findBy("authState", authState);
	}

	@Override
	public AuthorizationRequest findByAuthorizationCode(String authorizationCode) {
		return super.findBy("authorizationCode", authorizationCode);
	}

}
