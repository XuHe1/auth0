package io.spring2go.auth0.model;

public interface TokenResponseCache {
	
	VerifyTokenResponse getVerifyToken(String accessToken);

	void storeVerifyToken(String accessToken, VerifyTokenResponse tokenResponse);
	
}
