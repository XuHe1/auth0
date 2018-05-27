package io.spring2go.auth0.biz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import io.spring2go.auth0.Tester;
import io.spring2go.auth0.biz.OAuth2Validator.ValidationResponse;
import io.spring2go.auth0.model.AccessTokenRequest;
import io.spring2go.auth0.model.AuthorizationRequest;
import io.spring2go.auth0.model.Client;
import io.spring2go.auth0.service.ClientService;

@ActiveProfiles("test")
public class OAuth2ValidatorImplTest extends Tester {

	private AuthorizationRequest request;
	private Client client;

	@Autowired
	private ClientService clientService;

	@Autowired
	private OAuth2ValidatorImpl validator;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		this.client = createClient("client-app");
		when(clientService.findByUniqueClientId(client.getClientId())).thenReturn(client);
		this.request = getAuthorizationRequest(client);
	}

	@Test
	public void testValidateValidRedirectUri() {
		request.setRedirectUri("http://not-registered.com");
		validate(ValidationResponse.REDIRECT_URI_NOT_VALID);
	}

	@Test
	public void testInvalidRedirectUriFragment() {
		request.setRedirectUri(request.getRedirectUri() + "#fragment");
		validate(ValidationResponse.REDIRECT_URI_FRAGMENT_COMPONENT);
	}

	@Test
	public void testClientNotPermittedImplicitGrant() {
		request.setResponseType(OAuth2ValidatorImpl.IMPLICIT_GRANT_RESPONSE_TYPE);
		validate(ValidationResponse.IMPLICIT_GRANT_NOT_PERMITTED);
	}

	@Test
	public void testRedirectUriWithQueryParameter() {
		request.setRedirectUri(request.getRedirectUri() + "?param=example&param2=test");
		validate(ValidationResponse.VALID);
	}

	@Test
	public void testValidateClientId() {
		request.setUniqueClientId("unknown_client");
		validate(ValidationResponse.UNKNOWN_CLIENT_ID);
	}

	@Test
	public void testValidateImplicitGrant() {
		reset(clientService);
		Client client = createClient("client-app");
		client.setAllowedImplicitGrant(true);
		when(clientService.findByUniqueClientId(client.getClientId())).thenReturn(client);

		request.setResponseType(OAuth2ValidatorImpl.IMPLICIT_GRANT_RESPONSE_TYPE);
		request.setRedirectUri(" ");
		validate(ValidationResponse.IMPLICIT_GRANT_REDIRECT_URI);
	}

	@Test
	public void partOfRedirectUrimatching() {
		request.setRedirectUri("http://gothere.com.subdomain/extra_path");
		validate(ValidationResponse.VALID);
	}

	@Test
	public void testHappyFlow() {
		validate(ValidationResponse.VALID);
	}

	@Test
	public void testValidateResponseType() {
		request.setResponseType("not-existing-response-type");
		validate(ValidationResponse.UNSUPPORTED_RESPONSE_TYPE);
	}

	@Test
	public void testValidateScope() {
		request.setRequestedScopes("no-existing-scope");
		validate(ValidationResponse.SCOPE_NOT_VALID);
	}

	@Test
	public void testValidateRedirectUri() {
		request.setRedirectUri("qwert://no-valid-url");
		validate(ValidationResponse.REDIRECT_URI_NOT_URI);
	}

	@Test
	public void determineRedirectUri() {
		request.setRedirectUri("http://gothere.com");
		validator.determineRedirectUri(request, "code", createClient("clientId"));
		validate(ValidationResponse.VALID);
	}

	@Test
	public void implicitGrantNoRedirectGivenShouldUseDefault() {
		Client client = createClient("any");
		final String uri = "http://implicit-grant-uri/";
		request.setRedirectUri("");
		client.setRedirectUris(uri);
		try {
			final String determinedUri = validator.determineRedirectUri(request,
					OAuth2ValidatorImpl.IMPLICIT_GRANT_RESPONSE_TYPE, client);
			fail();
		} catch (ValidationResponseException e) {
			assertEquals(ValidationResponse.IMPLICIT_GRANT_REDIRECT_URI, e.v);
		}
	}

	@Test
	public void determineUrlValidImplicitGrant() {
		Client client = createClient("any");
		final String uri = "http://implicit-grant-uri/";
		request.setRedirectUri(uri);
		client.setRedirectUris(uri);
		final String determinedUri = validator.determineRedirectUri(request,
				OAuth2ValidatorImpl.IMPLICIT_GRANT_RESPONSE_TYPE, client);
		assertEquals(uri, determinedUri);
	}

	@Test
	public void testClientCredentialsTokenRequest() {
		AccessTokenRequest accessTokenRequest = new AccessTokenRequest();
		accessTokenRequest.setGrantType(OAuth2Validator.GRANT_TYPE_CLIENT_CREDENTIALS);
		accessTokenRequest.setUniqueClientId(client.getClientId());
		ValidationResponse response = validator.validate(accessTokenRequest);
		assertEquals(ValidationResponse.CLIENT_CREDENTIALS_NOT_PERMITTED, response);
		assertNull(accessTokenRequest.getClient());

		client.setAllowedClientCredentials(true);
		response = validator.validate(accessTokenRequest);
		assertEquals(ValidationResponse.VALID, response);
		assertEquals(client, accessTokenRequest.getClient());
	}

	private Client createClient(String clientId) {
		Client client = new Client();
		client.setClientName("Client App");
		client.setClientId(clientId);
		client.setRedirectUris("http://gothere.com,http://gohere.com");
		client.setScopes("read,update");
		return client;
	}

	private AuthorizationRequest getAuthorizationRequest(Client client) {
		AuthorizationRequest request = new AuthorizationRequest();
		request.setUniqueClientId(client.getClientId());
		request.setRedirectUri("http://gothere.com");
		request.setRequestedScopes("read,update");
		request.setResponseType(OAuth2ValidatorImpl.AUTHORIZATION_CODE_GRANT_RESPONSE_TYPE);
		return request;
	}

	private void validate(ValidationResponse expected) {
		ValidationResponse response = validator.validate(request);
		assertEquals(expected, response);
	}
}
