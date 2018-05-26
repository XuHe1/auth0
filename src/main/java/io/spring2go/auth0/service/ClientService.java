package io.spring2go.auth0.service;

import io.spring2go.auth0.core.Service;
import io.spring2go.auth0.model.Client;

public interface ClientService extends Service<Client> {
	
	public Client findByUniqueClientId(String clientId);

}
