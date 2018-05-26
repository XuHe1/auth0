package io.spring2go.auth0.noop;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.druid.util.StringUtils;

import io.spring2go.auth0.biz.AbstractUserConsentHandler;
import io.spring2go.auth0.model.Client;

/**
 * A noop implementation of {@link AbstractUserConsentHandler} that
 * contains no consent handling but only fulfills the contract of the
 * {@link UserConsentFilter}. Useful for testing and demonstration purposes
 * only, of course not safe for production.
 * 
 */
public class NoopUserConsentHandler extends AbstractUserConsentHandler {

	@Override
	public void handleUserConsent(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			String authStateValue, String returnUri, Client client) throws IOException, ServletException {
	    super.setAuthStateValue(request, authStateValue);
	    super.setGrantedScopes(request, StringUtils.isEmpty(client.getScopes()) ? new String[]{ } : client.getScopes().split("\\s*,\\s*"));
	    chain.doFilter(request, response);	
	}

}
