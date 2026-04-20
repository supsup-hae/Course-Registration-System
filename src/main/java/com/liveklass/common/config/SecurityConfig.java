package com.liveklass.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.liveklass.common.security.HeaderAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private static final String[] WHITELIST = {
		"/swagger-ui/**",
		"/swagger-ui.html",
		"/v3/api-docs/**",
		"/actuator/**"
	};

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(WHITELIST).permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/courses", "/api/v1/courses/**").permitAll()
				.anyRequest().authenticated()
			)
			.addFilterBefore(new HeaderAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
