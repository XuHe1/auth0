package io.spring2go.auth0.support;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.spring2go.auth0.model.AccessToken;
import io.spring2go.auth0.service.AccessTokenService;

/**
 * Helper class that contains scheduled tasks for database cleanup
 */
@Component
public class Cleaner {
	private static final Logger LOG = LoggerFactory.getLogger(Cleaner.class);

	@Autowired
	private AccessTokenService accessTokenService;

	/**
	 * Interval in ms between cleanup jobs
	 */
	private static final long CLEANUP_INTERVAL = 1000 * 3600;

	/**
	 * Throw away expired tokens after 30 days
	 */
	private static final long EXPIRED_TOKEN_CLEANUP_AGE = 1000L * 3600 * 24 * 30;

	@Scheduled(fixedDelay = CLEANUP_INTERVAL)
	public void cleanupExpiredAccessTokens() {
		LOG.debug("Cleaning up expired access tokens");
		for (AccessToken at : accessTokenService
				.findByMaxExpires(System.currentTimeMillis() - EXPIRED_TOKEN_CLEANUP_AGE)) {
			LOG.debug("Deleting expired access token {} (created: {}, expired: {})", at.getToken(),
					at.getCreatedTime(), new Date(at.getExpires()));
			accessTokenService.delete(at);
		}
	}
}
