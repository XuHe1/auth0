package io.spring2go.auth0.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "authorization_request")
public class AuthorizationRequest {
    /**
     * 自增主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 授权状态
     */
    @Column(name = "auth_state")
    private String authState;

    /**
     * 授权码
     */
    @Column(name = "authorization_code")
    private String authorizationCode;

    /**
     * 重定向Uri
     */
    @Column(name = "redirect_uri")
    private String redirectUri;

    /**
     * 响应类型
     */
    @Column(name = "response_type")
    private String responseType;

    /**
     * 状态参数
     */
    private String state;

    /**
     * 客户id引用
     */
    @Column(name = "client_id")
    private Long clientId;
    
    
    /**
     * 字符串形式的唯一客户id
     */
    @Transient
    private String uniqueClientId;

    /**
     * 授权的作用域列表，逗号隔开
     */
    @Column(name = "granted_scopes")
    private String grantedScopes;

    /**
     * 请求的作用域列表，逗号隔开
     */
    @Column(name = "requested_scopes")
    private String requestedScopes;

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
     * 授权请求所对应的Client
     */
    @Transient
    private Client client;

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
     * @param id 自增主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取授权状态
     *
     * @return auth_state - 授权状态
     */
    public String getAuthState() {
        return authState;
    }

    /**
     * 设置授权状态
     *
     * @param authState 授权状态
     */
    public void setAuthState(String authState) {
        this.authState = authState;
    }

    /**
     * 获取授权码
     *
     * @return authorization_code - 授权码
     */
    public String getAuthorizationCode() {
        return authorizationCode;
    }

    /**
     * 设置授权码
     *
     * @param authorizationCode 授权码
     */
    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    /**
     * 获取重定向Uri
     *
     * @return redirect_uri - 重定向Uri
     */
    public String getRedirectUri() {
        return redirectUri;
    }

    /**
     * 设置重定向Uri
     *
     * @param redirectUri 重定向Uri
     */
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    /**
     * 获取响应类型
     *
     * @return response_type - 响应类型
     */
    public String getResponseType() {
        return responseType;
    }

    /**
     * 设置响应类型
     *
     * @param responseType 响应类型
     */
    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    /**
     * 获取状态参数
     *
     * @return state - 状态参数
     */
    public String getState() {
        return state;
    }

    /**
     * 设置状态参数
     *
     * @param state 状态参数
     */
    public void setState(String state) {
        this.state = state;
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
     * @param clientId 客户id
     */
    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    /**
     * 获取授权的作用域列表，逗号隔开
     *
     * @return granted_scopes - 授权的作用域列表，逗号隔开
     */
    public String getGrantedScopes() {
        return grantedScopes;
    }

    /**
     * 设置授权的作用域列表，逗号隔开
     *
     * @param grantedScopes 授权的作用域列表，逗号隔开
     */
    public void setGrantedScopes(String grantedScopes) {
        this.grantedScopes = grantedScopes;
    }

    /**
     * 获取请求的作用域列表，逗号隔开
     *
     * @return requested_scopes - 请求的作用域列表，逗号隔开
     */
    public String getRequestedScopes() {
        return requestedScopes;
    }

    /**
     * 设置请求的作用域列表，逗号隔开
     *
     * @param requestedScopes 请求的作用域列表，逗号隔开
     */
    public void setRequestedScopes(String requestedScopes) {
        this.requestedScopes = requestedScopes;
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
     * @param createdTime 创建时间
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
     * @param modifiedTime 最后修改时间
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
     * @param encodedPrincipal 编码的主体
     */
    public void setEncodedPrincipal(String encodedPrincipal) {
        this.encodedPrincipal = encodedPrincipal;
    }
    
    /**
     * 设置授权请求所对应的Client
     * 
     * @param client 授权请求所对应的Client
     */
    public void setClient(Client client) {
    	this.client = client;
    }
    
    /**
     * 获取授权请求所对应的Client
     * 
     * @return client 授权请求所对应的Client
     */
    public Client getClient() {
    	return this.client;
    }

    
    /**
     * 获取唯一客户id
     * 
     * @return uniqueClientId  唯一客户id
     */
	public String getUniqueClientId() {
		return uniqueClientId;
	}

    /**
     * 设置唯一客户id
     * 
     * @param uniqueClientId 唯一客户id
     */
	public void setUniqueClientId(String uniqueClientId) {
		this.uniqueClientId = uniqueClientId;
	}
}