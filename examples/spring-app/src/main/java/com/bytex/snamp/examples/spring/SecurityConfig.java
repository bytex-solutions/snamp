package com.bytex.snamp.examples.spring;

import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    public void configure(final WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/hello-world*", "/passenger-manager*", "/driver-manager*");
    }
}
