package io.spring2go.auth0.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import io.spring2go.auth0.resource.RevokeResource;
import io.spring2go.auth0.resource.TokenResource;
import io.spring2go.auth0.resource.VerifyResource;
import io.spring2go.auth0.resource.mgmt.AccessTokenResource;
import io.spring2go.auth0.resource.mgmt.ClientResource;

@Component
public class JerseyConfig extends ResourceConfig
{
    public JerseyConfig()
    {
    	// oauth2 endpoints
        register(TokenResource.class);
        register(RevokeResource.class);
        register(VerifyResource.class);
        
		// management endpoints
		register(AccessTokenResource.class);
		register(ClientResource.class);
    }
}
