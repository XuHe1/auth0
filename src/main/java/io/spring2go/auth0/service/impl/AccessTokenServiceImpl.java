package io.spring2go.auth0.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.spring2go.auth0.core.AbstractService;
import io.spring2go.auth0.dao.AccessTokenMapper;
import io.spring2go.auth0.model.AccessToken;
import io.spring2go.auth0.service.AccessTokenService;
import tk.mybatis.mapper.entity.Condition;


/**
 * AccessToken Service Implementation
 */
@Service
@Transactional
public class AccessTokenServiceImpl extends AbstractService<AccessToken> implements AccessTokenService {
    @Resource
    private AccessTokenMapper accessTokenMapper;

	@Override
	public AccessToken findByToken(String token) {
		return super.findBy("token", token);
	}

	@Override
	public AccessToken findByTokenAndClientId(String token, Long clientId) {
		Condition condition = new Condition(AccessToken.class);
		condition.createCriteria().andEqualTo("token", token).andEqualTo("client_id", clientId);
		List<AccessToken> accessTokens = super.findByCondition(condition);
		
		return accessTokens.size() == 0 ? null : accessTokens.get(0);
	}

	@Override
	public AccessToken findByRefreshToken(String refreshToken) {
		return super.findBy("refreshToken", refreshToken);
	}

	@Override
	public List<AccessToken> findByResourceOwnerIdAndClientId(String resourceOwnerId, Long clientId) {
		Condition condition = new Condition(AccessToken.class);
		condition.createCriteria().andEqualTo("resource_owner_id", resourceOwnerId).andEqualTo("client_id", clientId);
		return super.findByCondition(condition);
	}

	@Override
	public List<AccessToken> findByResourceOwnerId(String resourceOwnerId) {
		Condition condition = new Condition(AccessToken.class);
		condition.createCriteria().andEqualTo("resource_owner_id", resourceOwnerId);
		return super.findByCondition(condition);
	}

	@Override
	public AccessToken findByIdAndResourceOwnerId(Long id, String ownerId) {
		Condition condition = new Condition(AccessToken.class);
		condition.createCriteria().andEqualTo("id", id).andEqualTo("resource_owner_id", ownerId);
		List<AccessToken> accessTokens = super.findByCondition(condition);
		
		return accessTokens.size() == 0 ? null : accessTokens.get(0);
	}
	
	@Override
	public int countByUniqueResourceOwnerIdAndClientId(long clientId) {
		Condition condition = new Condition(AccessToken.class);
		condition.setDistinct(true);
		condition.setCountProperty("resource_owner_id");
		condition.createCriteria().andEqualTo("client_id", clientId);
		return mapper.selectCountByCondition(condition);
	}

	@Override
	public void delete(AccessToken token) {
		mapper.delete(token);
	}

	@Override
	public List<AccessToken> findByMaxExpires(long expiresBoundary) {
		Condition condition = new Condition(AccessToken.class);
		condition.createCriteria().andGreaterThan("expires", 0).andLessThan("expires", expiresBoundary);
		
		return super.findByCondition(condition);
	}

}
