package io.spring2go.auth0.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ValidationErrorResponse {
	@JsonProperty
	private List<String> violations = new ArrayList<String>();

	public ValidationErrorResponse() {
	}

	public ValidationErrorResponse(List<String> violations) {
		this.violations = violations;
	}

	public List<String> getViolations() {
		return violations;
	}
}
