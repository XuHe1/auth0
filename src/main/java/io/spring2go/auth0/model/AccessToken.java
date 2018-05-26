package io.spring2go.auth0.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "access_token")
public class AccessToken {
	/**
	 * 自增主键
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 到期时间
	 */
	private Long expires;

	/**
	 * 刷新令牌
	 */
	@Column(name = "refresh_token")
	private String refreshToken;

	/**
	 * 资源拥有者Id
	 */
	@Column(name = "resource_owner_id")
	private String resourceOwnerId;

	/**
	 * 令牌
	 */
	private String token;

	/**
	 * 客户id
	 */
	@Column(name = "client_id")
	private Long clientId;

	/**
	 * 作用域列表，逗号隔开
	 */
	private String scopes;

	/**
	 * 创建时间
	 */
	@Column(name = "created_time")
	private Date createdTime;

	/**
	 * 最后修改时间
	 */
	@Column(name = "modified_time")
	private Date modifiedTime;

	/**
	 * 编码的主体
	 */
	@Column(name = "encoded_principal")
	private String encodedPrincipal;

	/**
	 * 获取自增主键
	 *
	 * @return id - 自增主键
	 */
	public Long getId() {
		return id;
	}

	/**
	 * 设置自增主键
	 *
	 * @param id
	 *            自增主键
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * 获取到期时间
	 *
	 * @return expires - 到期时间
	 */
	public Long getExpires() {
		return expires;
	}

	/**
	 * 设置到期时间
	 *
	 * @param expires
	 *            到期时间
	 */
	public void setExpires(Long expires) {
		this.expires = expires;
	}

	/**
	 * Nr of seconds relative to 'now', when token is to expire.
	 * 
	 * @see #getExpires()
	 */
	public long getExpiresIn() {
		if (expires == 0L) {
			return 0L;
		} else {
			long currInMs = System.currentTimeMillis();
			return Math.round((expires - currInMs) / 1000.0);
		}
	}

	/**
	 * 获取刷新令牌
	 *
	 * @return refresh_token - 刷新令牌
	 */
	public String getRefreshToken() {
		return refreshToken;
	}

	/**
	 * 设置刷新令牌
	 *
	 * @param refreshToken
	 *            刷新令牌
	 */
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	/**
	 * 获取资源拥有者Id
	 *
	 * @return resource_owner_id - 资源拥有者Id
	 */
	public String getResourceOwnerId() {
		return resourceOwnerId;
	}

	/**
	 * 设置资源拥有者Id
	 *
	 * @param resourceOwnerId
	 *            资源拥有者Id
	 */
	public void setResourceOwnerId(String resourceOwnerId) {
		this.resourceOwnerId = resourceOwnerId;
	}

	/**
	 * 获取令牌
	 *
	 * @return token - 令牌
	 */
	public String getToken() {
		return token;
	}

	/**
	 * 设置令牌
	 *
	 * @param token
	 *            令牌
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * 获取客户id
	 *
	 * @return client_id - 客户id
	 */
	public Long getClientId() {
		return clientId;
	}

	/**
	 * 设置客户id
	 *
	 * @param clientId
	 *            客户id
	 */
	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	/**
	 * 获取作用域列表，逗号隔开
	 *
	 * @return scopes - 作用域列表，逗号隔开
	 */
	public String getScopes() {
		return scopes;
	}

	/**
	 * 设置作用域列表，逗号隔开
	 *
	 * @param scopes
	 *            作用域列表，逗号隔开
	 */
	public void setScopes(String scopes) {
		this.scopes = scopes;
	}

	/**
	 * 获取创建时间
	 *
	 * @return created_time - 创建时间
	 */
	public Date getCreatedTime() {
		return createdTime;
	}

	/**
	 * 设置创建时间
	 *
	 * @param createdTime
	 *            创建时间
	 */
	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	/**
	 * 获取最后修改时间
	 *
	 * @return modified_time - 最后修改时间
	 */
	public Date getModifiedTime() {
		return modifiedTime;
	}

	/**
	 * 设置最后修改时间
	 *
	 * @param modifiedTime
	 *            最后修改时间
	 */
	public void setModifiedTime(Date modifiedTime) {
		this.modifiedTime = modifiedTime;
	}

	/**
	 * 获取编码的主体
	 *
	 * @return encoded_principal - 编码的主体
	 */
	public String getEncodedPrincipal() {
		return encodedPrincipal;
	}

	/**
	 * 设置编码的主体
	 *
	 * @param encodedPrincipal
	 *            编码的主体
	 */
	public void setEncodedPrincipal(String encodedPrincipal) {
		this.encodedPrincipal = encodedPrincipal;
	}
}