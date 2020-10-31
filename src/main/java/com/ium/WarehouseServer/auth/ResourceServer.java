package com.ium.WarehouseServer.auth;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@Configuration
@EnableResourceServer
public class ResourceServer extends ResourceServerConfigurerAdapter {
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(HttpMethod.GET, "/instruments").hasRole("USER")
                .antMatchers(HttpMethod.POST, "/instruments/**").hasRole("USER")
                .antMatchers(HttpMethod.PUT, "/instruments/**").hasRole("USER")
                .antMatchers(HttpMethod.DELETE, "/instruments/**").hasRole("MANAGER")
                .antMatchers("/").permitAll();
    }
}

