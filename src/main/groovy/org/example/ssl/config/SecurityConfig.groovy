package org.example.ssl.config

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.security.Http401AuthenticationEntryPoint
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Slf4j
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value('${spring.ldap.username?:}')
    String managerDn

    @Value('${spring.ldap.password?:}')
    String managerPassword

    @Value('${spring.ldap.urls?:}')
    String ldapUrl

    @Value('${embeddedLdapEnabled:true}')
    boolean embeddedLdapEnabled

    @Value('${spring.ldap.embedded.port:8389}')
    String embeddedLdapPort

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and().exceptionHandling()
                .defaultAuthenticationEntryPointFor(new Http401AuthenticationEntryPoint(), new AntPathRequestMatcher('/api'))
                .and().authorizeRequests()
                .antMatchers("/api/v1.0/user/login").permitAll()
                .antMatchers("/api/**").authenticated()
                .and().httpBasic()
                .and().cors()

        http.logout()
                .logoutUrl('/api/v1.0/logout')
                .invalidateHttpSession(true)
                .deleteCookies('JSESSIONID', 'XSRF-TOKEN')
                .logoutSuccessHandler((new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)))

        if (embeddedLdapEnabled) {
            log.info("\n\n TURNING OFF CSRF, ALLOWING FRAME OPTIONS FROM SAME ORIGIN (h2 console) \n\n")
            //LOCAL DEV
            http.csrf().disable()
            http.headers().frameOptions().sameOrigin()
        }
    }

    @Override
    void configure(final WebSecurity web) throws Exception {
        web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**")
    }

    @Bean
    FilterRegistrationBean registerCorsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource()
        CorsConfiguration config = new CorsConfiguration()
        config.setAllowCredentials(true)
        config.addAllowedOrigin("*")
        config.addAllowedHeader("*")
        config.addAllowedMethod("*")
        source.registerCorsConfiguration("/**", config)
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source))
        bean.setOrder(0)
        return bean
    }

    @Override
    void configure(AuthenticationManagerBuilder auth) throws Exception {

        if (embeddedLdapEnabled) {
            log.info("\n\n USING EMBEDDED LDAP \n\n")
            //LOCAL DEV
            auth
                    .ldapAuthentication()
                    .userDnPatterns("uid={0},ou=people")
                    .groupSearchBase("ou=groups")
                    .contextSource()
                    .url("ldap://localhost:${embeddedLdapPort}/dc=springframework,dc=org")
                    .and()
                    .passwordCompare()
                    .passwordAttribute("userPassword")
        } else {
            auth
                    .ldapAuthentication()
                    .userSearchBase('...')
                    .groupSearchBase('...')
                    .groupSearchFilter('(member={0})')
                    .ldapAuthoritiesPopulator()
                    .contextSource().url(ldapUrl)
                    .managerDn(managerDn).managerPassword(managerPassword)
        }
    }
}
