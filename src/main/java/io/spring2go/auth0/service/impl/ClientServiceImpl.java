package io.spring2go.auth0.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.spring2go.auth0.core.AbstractService;
import io.spring2go.auth0.model.Client;
import io.spring2go.auth0.service.ClientService;

/**
 * Client Service Implementation
 */
@Service
@Transactional
public class ClientServiceImpl extends AbstractService<Client> implements ClientService {

	@Override
	public Client findByUniqueClientId(String clientId) {
		return super.findBy("clientId", clientId);
	}

}
