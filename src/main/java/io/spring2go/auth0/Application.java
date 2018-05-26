package io.spring2go.auth0;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import io.spring2go.auth0.authentication.FormLoginAuthenticator;
import io.spring2go.auth0.biz.AuthenticationFilter;
import io.spring2go.auth0.biz.AuthorizationServerFilter;
import io.spring2go.auth0.biz.OAuth2Validator;
import io.spring2go.auth0.biz.UserConsentFilter;
import io.spring2go.auth0.consent.FormUserConsentHandler;
import io.spring2go.auth0.service.AuthorizationRequestService;
import io.spring2go.auth0.service.ClientService;

/**
 * The SpringConfiguration is a {@link Configuration} that can be overridden if
 * you want to plugin your own implementations. Note that the two most likely
 * candidates to change are the {@link AbstractAuthenticator} an
 * {@link AbstractUserConsentHandler}. You can change the implementation by
 * editing the application.apis.properties file where the implementations are
 * configured.
 */
@Configuration
@ComponentScan(basePackages = { "io.spring2go.auth0" })
@EnableAutoConfiguration
@SpringBootApplication
public class Application {

	@Value("${authenticatorClass}")
	String authenticatorClassName;

	@Value("${userConsentHandlerClass}")
	String userConsentHandlerClassName;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public FilterRegistrationBean adminAuthorizationFilter(
			@Value("${adminService.tokenVerificationUrl}") String authorizationServerUrl,
			@Value("${adminService.jsonTypeInfoIncluded:false}") boolean jsonTypeInfoIncluded,
			@Value("${adminService.cacheEnabled:false}") boolean cacheEnabled,
			@Value("${adminService.allowCorsRequests:true}") boolean allowCorsRequests) {
		FilterRegistrationBean bean = new FilterRegistrationBean();
		AuthorizationServerFilter authorizationServerFilter = new AuthorizationServerFilter(authorizationServerUrl);
		authorizationServerFilter.setAllowCorsRequests(allowCorsRequests);
		authorizationServerFilter.setCacheEnabled(cacheEnabled);
		authorizationServerFilter.setTypeInformationIsIncluded(jsonTypeInfoIncluded);
		bean.setFilter(authorizationServerFilter);
		bean.addUrlPatterns("/admin/*");
		return bean;
	}

	@Bean
	public FilterRegistrationBean oauth2AuthenticationFilter(AuthorizationRequestService authorizationRequestService,
			FormLoginAuthenticator authenticator, OAuth2Validator oAuth2Validator) {
		FilterRegistrationBean bean = new FilterRegistrationBean();
		AuthenticationFilter filter = new AuthenticationFilter(authenticator, authorizationRequestService,
				oAuth2Validator);
		bean.setFilter(filter);
		bean.addUrlPatterns("/oauth2/authorize");
		bean.setOrder(1);
		return bean;
	}

	@Bean
	public FilterRegistrationBean oauth2UserConsentFilter(AuthorizationRequestService authorizationRequestService,
			ClientService clientService, FormUserConsentHandler userConsentHandler) {
		FilterRegistrationBean bean = new FilterRegistrationBean();
		UserConsentFilter filter = new UserConsentFilter(authorizationRequestService, clientService,
				userConsentHandler);
		bean.setFilter(filter);
		bean.addUrlPatterns("/oauth2/authorize", "/oauth2/consent");
		bean.setOrder(2);
		return bean;
	}

	@Bean
	public FormLoginAuthenticator authenticator() {
		return new FormLoginAuthenticator();
	}
}
