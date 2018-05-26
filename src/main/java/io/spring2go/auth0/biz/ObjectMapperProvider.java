package io.spring2go.auth0.biz;

import javax.ws.rs.client.Client;
import javax.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * We need to be able to set the {@link ObjectMapper} on the {@link Client} to
 * make sure the {@link MrBeanModule} is used.
 *
 */
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

	private ObjectMapper mapper;

	public ObjectMapperProvider() {
		mapper = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
				.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL)
				.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ws.rs.ext.ContextResolver#getContext(java.lang.Class)
	 */
	@Override
	public ObjectMapper getContext(Class<?> type) {
		return mapper;
	}

}
