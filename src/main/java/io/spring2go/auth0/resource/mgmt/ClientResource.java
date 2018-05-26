package io.spring2go.auth0.resource.mgmt;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.spring2go.auth0.model.Client;
import io.spring2go.auth0.service.ClientService;

/**
 * JAX-RS Resource for CRUD operations on Clients. (clients in OAuth 2 context).
 */
@Path("/admin/client")
@Produces(MediaType.APPLICATION_JSON)
@Component
public class ClientResource extends AbstractResource {
	private static final Logger LOG = LoggerFactory.getLogger(ClientResource.class);
	private static final String FILTERED_CLIENT_ID_CHARS = "[^a-z0-9_\\x2D]";

	@Autowired
	private ClientService clientService;

	/**
	 * Get a list of all clients auth0 manages
	 */
	@GET
	public Response getAll(@Context HttpServletRequest request) {
		Response validateScopeResponse = validateScope(request, Collections.singletonList(AbstractResource.SCOPE_READ));
		if (validateScopeResponse != null) {
			return validateScopeResponse;
		}
		List<Client> clients = clientService.findAll();
		return response(clients);
	}

	/**
	 * Get a specific Client.
	 */
	@GET
	@Path("/{clientId}")
	public Response getById(@Context HttpServletRequest request, @PathParam("clientId") Long id) {
		Response validateScopeResponse = validateScope(request, Collections.singletonList(AbstractResource.SCOPE_READ));
		if (validateScopeResponse != null) {
			return validateScopeResponse;
		}
		Client client = clientService.findById(id);
		return response(client);
	}

	/**
	 * Save a new client.
	 */
	@PUT
	public Response put(@Context HttpServletRequest request, Client client) {

		Response validateScopeResponse = validateScope(request,
				Collections.singletonList(AbstractResource.SCOPE_WRITE));
		if (validateScopeResponse != null) {
			return validateScopeResponse;
		}

		client.setClientId(generateUniqueClientId(client));
		client.setSecret(client.isAllowedImplicitGrant() ? null : generateSecret());

		try {
			clientService.save(client);
		} catch (RuntimeException e) {
			return buildErrorResponse(e);
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Saved client: {}", client);
		}
		final URI uri = UriBuilder.fromPath("{clientId}.json").build(client.getId());
		return Response.created(uri).entity(client).build();
	}

	protected String generateSecret() {
		return super.generateRandom();
	}

	/**
	 * Delete a given client.
	 */
	@DELETE
	@Path("/{clientId}")
	public Response delete(@Context HttpServletRequest request, @PathParam("clientId") Long id) {

		Response validateScopeResponse = validateScope(request,
				Collections.singletonList(AbstractResource.SCOPE_WRITE));
		if (validateScopeResponse != null) {
			return validateScopeResponse;
		}

		Client client = clientService.findById(id);

		if (client == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Deleting client: {}", client);
		}
		clientService.deleteById(id);
		return Response.noContent().build();
	}

	/**
	 * Update an existing client.
	 */
	@POST
	@Path("/{clientId}")
	public Response post(@Valid Client newOne, @PathParam("clientId") Long id, @Context HttpServletRequest request) {

		Response validateScopeResponse = validateScope(request,
				Collections.singletonList(AbstractResource.SCOPE_WRITE));
		if (validateScopeResponse != null) {
			return validateScopeResponse;
		}

		final Client clientFromStore = clientService.findById(id);
		if (clientFromStore == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		// Copy over read-only fields
		newOne.setClientId(clientFromStore.getClientId());
		newOne.setSecret(newOne.isAllowedImplicitGrant() ? null : clientFromStore.getSecret());

		try {
			clientService.save(newOne);
		} catch (RuntimeException e) {
			return buildErrorResponse(e);
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Saving client: {}", newOne);
		}
		return Response.ok(newOne).build();
	}

	/**
	 * Method that generates a unique client id, taking into account existing
	 * clientIds in the backend.
	 *
	 * @param client
	 *            the client for whom to generate an id.
	 * @return the generated id. Callers are responsible themselves for actually
	 *         calling {@link Client#setClientId(String)}
	 */
	protected String generateUniqueClientId(Client client) {
		String clientId = sanitizeClientName(client.getClientName());
		if (clientService.findByUniqueClientId(clientId) != null) {

			String baseClientId = clientId;

			/*
			 * if one with such name exists already, the next one would actually be number
			 * 2. Therefore, start counting with 2.
			 */
			int i = 2;
			do {
				clientId = baseClientId + (i++);
			} while (clientService.findByUniqueClientId(clientId) != null);
		}
		return clientId;
	}

	protected String sanitizeClientName(String name) {
		return name.toLowerCase().replaceAll(" ", "-").replaceAll(FILTERED_CLIENT_ID_CHARS, "");
	}
}
