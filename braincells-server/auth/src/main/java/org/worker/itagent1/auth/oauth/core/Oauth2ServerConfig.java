package org.worker.itagent1.auth.oauth.core;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

/**
 * @author itagent1.worker
 * @version 1.0
 * @ClassName AuthorizationServerConfig
 * @description: Oauth2 认证服务配置
 * https://zhuanlan.zhihu.com/p/612502443
 * @date 2023/6/4 23:37
 */
@AllArgsConstructor
@Configuration
@EnableAuthorizationServer
public class Oauth2ServerConfig extends AuthorizationServerConfigurerAdapter {
    // 认证管理器
    @Autowired
    private AuthenticationManager authenticationManager;

    // 数据源
    @Autowired
    private DataSource dataSource;

    //密码加密方式
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 自定义身份认证
    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public ClientDetailsService jdbcClientDetailsService(){
        //存储client信息
        return new JdbcClientDetailsService(dataSource);
    }
    @Bean
    public TokenStore tokenStore(){
        // token存储
        return new JdbcTokenStore(dataSource);
    }
    @Bean
    public AuthorizationCodeServices authorizationCodeServices() {
        // 授权码模式
        return new JdbcAuthorizationCodeServices(dataSource);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .tokenStore(tokenStore())//token存储方式
                .authenticationManager(authenticationManager)// 开启密码验证，由WebSecurityConfigurerAdapter
                .userDetailsService(userDetailsService)// 读取验证用户信息
                .authorizationCodeServices(authorizationCodeServices())
                .setClientDetailsService(jdbcClientDetailsService());
    }
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        //  配置Endpoint,允许请求
        security.tokenKeyAccess("permitAll()") // 开启/oauth/token_key 验证端口-无权限
                .checkTokenAccess("isAuthenticated()") // 开启/oauth/check_token 验证端口-需权限
                .allowFormAuthenticationForClients()// 允许表单认证
                .passwordEncoder(passwordEncoder());   // 配置BCrypt加密
    }
}
