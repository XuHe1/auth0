package io.spring2go.auth0.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representation an error response conform <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2#section-5.2">spec</a>
 * 
 */
public class ErrorResponse {
	private String error;
	@JsonProperty("error_description")
	private String errorDescription;
	
	public ErrorResponse() {
		super();
	}
	
	public ErrorResponse(String error, String errorDescription) {
		super();
		this.error = error;
		this.errorDescription = errorDescription;
	}
	
	/**
	 * @return the error
	 */
	public String getError() {
		return error;
	}
	
	/**
	 * @param error
	 *          the error to set
	 */
	public void setError(String error) {
		this.error = error;
	}
	
	/**
	 * @return the errorDescription
	 */
	public String getErrorDescription() {
		return errorDescription;
	}
	
	/**
	 * @param errorDescription
	 *           the errorDescription to set
	 */
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
}
