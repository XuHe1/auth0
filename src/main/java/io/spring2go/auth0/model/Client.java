package io.spring2go.auth0.model;

import java.util.Date;
import javax.persistence.*;

import io.spring2go.auth0.principal.UserPassCredentials;

public class Client {
	/**
	 * 自增主键
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 客户id
	 */
	@Column(name = "client_id")
	private String clientId;

	/**
	 * 联系人email
	 */
	@Column(name = "contact_email")
	private String contactEmail;

	/**
	 * 联系人姓名
	 */
	@Column(name = "contact_name")
	private String contactName;

	/**
	 * 描述
	 */
	private String description;

	/**
	 * 到期持续时间
	 */
	@Column(name = "expire_duration")
	private Long expireDuration;

	/**
	 * 客户名称
	 */
	@Column(name = "client_name")
	private String clientName;

	/**
	 * 是否允许简化模式
	 */
	@Column(name = "allowed_implicit_grant")
	private Boolean allowedImplicitGrant;

	/**
	 * 是否允许客户名密码模式
	 */
	@Column(name = "allowed_client_credentials")
	private Boolean allowedClientCredentials;

	/**
	 * 客户密码
	 */
	private String secret;

	/**
	 * 是否忽略同意
	 */
	@Column(name = "skip_consent")
	private Boolean skipConsent;

	/**
	 * 是否包含主体信息
	 */
	@Column(name = "include_principal")
	private Boolean includePrincipal;

	/**
	 * 小图标Url
	 */
	@Column(name = "thumb_nail_url")
	private String thumbNailUrl;

	/**
	 * 是否支持刷新令牌
	 */
	@Column(name = "use_refresh_tokens")
	private Boolean useRefreshTokens;

	/**
	 * 重定向uris，逗号隔开
	 */
	@Column(name = "redirect_uris")
	private String redirectUris;

	/**
	 * 作用域列表，逗号隔开
	 */
	private String scopes;

	/**
	 * 额外属性，键值对以逗号隔开
	 */
	private String attributes;

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
	 * 获取客户id
	 *
	 * @return client_id - 客户id
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * 设置客户id
	 *
	 * @param clientId
	 *            客户id
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * 获取联系人email
	 *
	 * @return contact_email - 联系人email
	 */
	public String getContactEmail() {
		return contactEmail;
	}

	/**
	 * 设置联系人email
	 *
	 * @param contactEmail
	 *            联系人email
	 */
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	/**
	 * 获取联系人姓名
	 *
	 * @return contact_name - 联系人姓名
	 */
	public String getContactName() {
		return contactName;
	}

	/**
	 * 设置联系人姓名
	 *
	 * @param contactName
	 *            联系人姓名
	 */
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	/**
	 * 获取描述
	 *
	 * @return description - 描述
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 设置描述
	 *
	 * @param description
	 *            描述
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 获取到期持续时间
	 *
	 * @return expire_duration - 到期持续时间
	 */
	public Long getExpireDuration() {
		return expireDuration;
	}

	/**
	 * 设置到期持续时间
	 *
	 * @param expireDuration
	 *            到期持续时间
	 */
	public void setExpireDuration(Long expireDuration) {
		this.expireDuration = expireDuration;
	}

	/**
	 * 获取客户名称
	 *
	 * @return client_name - 客户名称
	 */
	public String getClientName() {
		return clientName;
	}

	/**
	 * 设置客户名称
	 *
	 * @param clientName
	 *            客户名称
	 */
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	/**
	 * 获取是否允许简化模式
	 *
	 * @return allowed_implicit_grant - 是否允许简化模式
	 */
	public Boolean isAllowedImplicitGrant() {
		return allowedImplicitGrant;
	}

	/**
	 * 设置是否允许简化模式
	 *
	 * @param allowedImplicitGrant
	 *            是否允许简化模式
	 */
	public void setAllowedImplicitGrant(Boolean allowedImplicitGrant) {
		this.allowedImplicitGrant = allowedImplicitGrant;
	}

	/**
	 * 获取是否允许客户名密码模式
	 *
	 * @return allowed_client_credentials - 是否允许客户名密码模式
	 */
	public Boolean isAllowedClientCredentials() {
		return allowedClientCredentials;
	}

	/**
	 * 设置是否允许客户名密码模式
	 *
	 * @param allowedClientCredentials
	 *            是否允许客户名密码模式
	 */
	public void setAllowedClientCredentials(Boolean allowedClientCredentials) {
		this.allowedClientCredentials = allowedClientCredentials;
	}

	/**
	 * 获取客户密码
	 *
	 * @return secret - 客户密码
	 */
	public String getSecret() {
		return secret;
	}

	/**
	 * 设置客户密码
	 *
	 * @param secret
	 *            客户密码
	 */
	public void setSecret(String secret) {
		this.secret = secret;
	}

	/**
	 * 获取是否忽略同意
	 *
	 * @return skip_consent - 是否忽略同意
	 */
	public Boolean isSkipConsent() {
		return skipConsent;
	}

	/**
	 * 设置是否忽略同意
	 *
	 * @param skipConsent
	 *            是否忽略同意
	 */
	public void setSkipConsent(Boolean skipConsent) {
		this.skipConsent = skipConsent;
	}

	/**
	 * 获取是否包含主体信息
	 *
	 * @return include_principal - 是否包含主体信息
	 */
	public Boolean isIncludePrincipal() {
		return includePrincipal;
	}

	/**
	 * 设置是否包含主体信息
	 *
	 * @param includePrincipal
	 *            是否包含主体信息
	 */
	public void setIncludePrincipal(Boolean includePrincipal) {
		this.includePrincipal = includePrincipal;
	}

	/**
	 * 获取小图标Url
	 *
	 * @return thumb_nail_url - 小图标Url
	 */
	public String getThumbNailUrl() {
		return thumbNailUrl;
	}

	/**
	 * 设置小图标Url
	 *
	 * @param thumbNailUrl
	 *            小图标Url
	 */
	public void setThumbNailUrl(String thumbNailUrl) {
		this.thumbNailUrl = thumbNailUrl;
	}

	/**
	 * 获取是否支持刷新令牌
	 *
	 * @return use_refresh_tokens - 是否支持刷新令牌
	 */
	public Boolean isUseRefreshTokens() {
		return useRefreshTokens;
	}

	/**
	 * 设置是否支持刷新令牌
	 *
	 * @param useRefreshTokens
	 *            是否支持刷新令牌
	 */
	public void setUseRefreshTokens(Boolean useRefreshTokens) {
		this.useRefreshTokens = useRefreshTokens;
	}

	/**
	 * 获取重定向uris，逗号隔开
	 *
	 * @return redirect_uris - 重定向uris，逗号隔开
	 */
	public String getRedirectUris() {
		return redirectUris;
	}

	/**
	 * 设置重定向uris，逗号隔开
	 *
	 * @param redirectUris
	 *            重定向uris，逗号隔开
	 */
	public void setRedirectUris(String redirectUris) {
		this.redirectUris = redirectUris;
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
	 * 获取额外属性，键值对以逗号隔开
	 *
	 * @return attributes - 额外属性，键值对以逗号隔开
	 */
	public String getAttributes() {
		return attributes;
	}

	/**
	 * 设置额外属性，键值对以逗号隔开
	 *
	 * @param attributes
	 *            额外属性，键值对以逗号隔开
	 */
	public void setAttributes(String attributes) {
		this.attributes = attributes;
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

	public boolean isExactMatch(UserPassCredentials credentials) {
		return credentials != null && credentials.isValid() && credentials.getUsername().equals(clientId)
				&& credentials.getPassword().equals(secret);

	}
}