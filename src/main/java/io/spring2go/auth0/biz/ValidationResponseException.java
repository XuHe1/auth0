package io.spring2go.auth0.biz;

import io.spring2go.auth0.biz.OAuth2Validator.ValidationResponse;

@SuppressWarnings("serial")
public class ValidationResponseException extends RuntimeException {

	public final ValidationResponse v;

	public ValidationResponseException(ValidationResponse v) {
		this.v = v;
	}

}
