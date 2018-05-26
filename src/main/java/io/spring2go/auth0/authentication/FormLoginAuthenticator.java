package io.spring2go.auth0.authentication;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;

import io.spring2go.auth0.biz.AbstractAuthenticator;
import io.spring2go.auth0.principal.AuthenticatedPrincipal;

/**
 * {@link AbstractAuthenticator} that redirects to a form. Note that other
 * implementations can go wild because they have access to the
 * {@link HttpServletRequest} and {@link HttpServletResponse}.
 * 
 */
@Component
public class FormLoginAuthenticator extends AbstractAuthenticator  {
	  private static final String SESSION_IDENTIFIER = "AUTHENTICATED_PRINCIPAL";

	  @Override
	  public boolean canCommence(HttpServletRequest request) {
	    return request.getMethod().equals("POST") && request.getParameter(AUTH_STATE) != null
	        && request.getParameter("j_username") != null;
	  }

	  @Override
	  public void authenticate(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
	      String authStateValue, String returnUri) throws IOException, ServletException {
	    HttpSession session = request.getSession(false);
	    AuthenticatedPrincipal principal = (AuthenticatedPrincipal) (session != null ? session
	        .getAttribute(SESSION_IDENTIFIER) : null);
	    if (request.getMethod().equals("POST")) {
	      processForm(request);
	      chain.doFilter(request, response);
	    } else if (principal != null) {
	      // we stil have the session
	      setAuthStateValue(request, authStateValue);
	      setPrincipal(request, principal);
	      chain.doFilter(request, response);
	    } else {
	      processInitial(request, response, returnUri, authStateValue);
	    }
	  }

	  private void processInitial(HttpServletRequest request, ServletResponse response, String returnUri,
	      String authStateValue) throws IOException, ServletException {
	    request.setAttribute(AUTH_STATE, authStateValue);
	    request.setAttribute("actionUri", returnUri);
	    request.getRequestDispatcher("/login.html").forward(request, response);
	  }

	  /**
	   * 
	   * Hook for actually validating the username/ password against a database,
	   * ldap, external webservice or whatever to perform authentication
	   * 
	   * @param request
	   *          the {@link HttpServletRequest}
	   */
	  protected void processForm(final HttpServletRequest request) {
	    setAuthStateValue(request, request.getParameter(AUTH_STATE));
	    AuthenticatedPrincipal principal = new AuthenticatedPrincipal(request.getParameter("j_username"));
	    request.getSession().setAttribute(SESSION_IDENTIFIER, principal);
	    setPrincipal(request, principal);
	  }
}
