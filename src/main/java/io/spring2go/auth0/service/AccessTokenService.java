package io.spring2go.auth0.service;
import java.util.List;

import io.spring2go.auth0.core.Service;
import io.spring2go.auth0.model.AccessToken;

/**
 * Created by CodeGenerator on 2018/05/24.
 */
public interface AccessTokenService extends Service<AccessToken> {
	
	public AccessToken findByToken(String token);
	
	public AccessToken findByTokenAndClientId(String token, Long clientId);
	
	public AccessToken findByRefreshToken(String refreshToken);
	
	public List<AccessToken> findByResourceOwnerIdAndClientId(String resourceOwnerId, Long clientId);
	
	public List<AccessToken> findByResourceOwnerId(String resourceOwnerId);
	
	public  AccessToken findByIdAndResourceOwnerId(Long id, String owner);
	
	public int countByUniqueResourceOwnerIdAndClientId(long clientId);
	
	public void delete(AccessToken token);
	
	public List<AccessToken> findByMaxExpires(long expiresBoundary);
}
