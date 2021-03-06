package com.mj.securitystudy.config;

import com.mj.securitystudy.filter.AuthoritiesLoggingAfterFilter;
import com.mj.securitystudy.filter.AuthoritiesLoggingAtFilter;
import com.mj.securitystudy.filter.JWTTokenValidatorFilter;
import com.mj.securitystudy.filter.JwtTokenGeneratorFilter;
import com.mj.securitystudy.filter.RequestValidationBeforeFilter;
import java.util.Arrays;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    /*
    /my-account - secured
    /my-balance - secured
    /my-loans   - secured
    /my-cards   - secured

    /notices    - not secured
    /contact    - not secured
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .cors().configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
                config.setAllowedMethods(Collections.singletonList("*"));
                config.setAllowCredentials(true);
                config.setAllowedHeaders(Collections.singletonList("*"));
                config.setExposedHeaders(Arrays.asList("Authorization"));
                config.setMaxAge(3600L);
                return config;
            }).and()
            .csrf().disable()
            /*
            .ignoringAntMatchers("/contact")
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
             */
            .addFilterBefore(new RequestValidationBeforeFilter(), BasicAuthenticationFilter.class)
            .addFilterAfter(new AuthoritiesLoggingAfterFilter(), BasicAuthenticationFilter.class)
            .addFilterAt(new AuthoritiesLoggingAtFilter(), BasicAuthenticationFilter.class)
            .addFilterAfter(new JwtTokenGeneratorFilter(), BasicAuthenticationFilter.class)
            .addFilterBefore(new JWTTokenValidatorFilter(), BasicAuthenticationFilter.class)
            .authorizeRequests()
            .antMatchers("/my-account").hasRole("USER")
            .antMatchers("/my-balance").hasAnyRole("USER", "ADMIN")
            .antMatchers("/my-loans").hasRole("ROOT")
            .antMatchers("/my-cards").hasAnyRole("USER", "ADMIN")
//            .antMatchers("/user").authenticated()
            .antMatchers("/notices").permitAll()
            .antMatchers("/contact").permitAll()
            // deny
            .antMatchers("/deny").denyAll().and()
            .formLogin().and()
            .httpBasic();
    }

    /*@Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
            .withUser("admin")
            .password("1234")
            .authorities("admin")
            .and()
            .withUser("user")
            .password("1234")
            .authorities("read")
            .and()
            .passwordEncoder(NoOpPasswordEncoder.getInstance());
    }*/

    /*@Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager();
        UserDetails user1 = User.withUsername("admin").password("1111").authorities("admin")
            .build();
        UserDetails user2 = User.withUsername("user").password("1111").authorities("read").build();
        userDetailsService.createUser(user1);
        userDetailsService.createUser(user2);
        auth.userDetailsService(userDetailsService);
    }*/

    /*@Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }*/

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
