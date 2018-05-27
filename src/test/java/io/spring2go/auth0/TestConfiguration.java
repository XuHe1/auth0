package io.spring2go.auth0;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.spring2go.auth0.service.ClientService;

import org.springframework.context.annotation.Primary;

@Profile("test")
@Configuration
public class TestConfiguration {
	@Bean
	@Primary
	public ClientService clientService() {
		return Mockito.mock(ClientService.class);
	}
}
